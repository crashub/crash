/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.crsh.console.jline;

import jline.Terminal;
import jline.console.ConsoleReader;
import jline.console.KeyMap;
import jline.console.Operation;
import jline.internal.NonBlockingInputStream;
import org.crsh.console.Console;
import org.crsh.console.ConsoleDriver;
import org.crsh.shell.Shell;
import org.crsh.text.Style;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.PrintStream;
import java.util.Stack;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JLineProcessor implements Runnable, ConsoleDriver {

  /** . */
  private final Console console;

  /** Whether or not we switched on the alternate screen. */
  boolean useAlternate;

  // *********

  final CountDownLatch done;
  final Terminal terminal;
  final PrintStream writer;
  final ConsoleReader reader;
  final String lineSeparator;
  final boolean ansi;
  final Logger logger = Logger.getLogger(JLineProcessor.class.getName());

  public JLineProcessor(
      boolean ansi,
      Shell shell,
      ConsoleReader reader,
      PrintStream out) {
    this(ansi, shell, reader, out, System.getProperty("line.separator"));
  }

  public JLineProcessor(
      boolean ansi,
      Shell shell,
      final ConsoleReader reader,
      PrintStream out,
      String lineSeparator) {

    //
    this.console = new Console(shell, this);
    this.writer = out;
    this.useAlternate = false;
    this.terminal = reader.getTerminal();
    this.reader = reader;
    this.lineSeparator = lineSeparator;
    this.done = new CountDownLatch(1);
    this.ansi = ansi;

    // Update the mode according to the notification
    console.addModeListener(new Runnable() {
      @Override
      public void run() {
        reader.setKeyMap(console.getMode().getKeyMap());
      }
    });
  }

  public void interrupt() {
    console.on(Operation.INTERRUPT);
  }

  // *****

  public void closed() throws InterruptedException {
    done.await();
  }

  public void run() {

    //
    int escapeTimeout = 100;

    //
    console.init();
    StringBuilder sb = new StringBuilder();
    Stack<Character> pushBackChar = new Stack<Character>();
    while (console.isRunning()) {
      try {

        //
        int c = pushBackChar.isEmpty() ? reader.readCharacter() : pushBackChar.pop ();
        if (c == -1) {
          break;
        }

        //
        sb.appendCodePoint(c);

        //
        Object o = reader.getKeys().getBound( sb );

        /*
         * A KeyMap indicates that the key that was struck has a
         * number of keys that can follow it as indicated in the
         * map. This is used primarily for Emacs style ESC-META-x
         * lookups. Since more keys must follow, go back to waiting
         * for the next key.
         */
        if ( o instanceof KeyMap) {
          /*
           * The ESC key (#27) is special in that it is ambiguous until
           * you know what is coming next.  The ESC could be a literal
           * escape, like the user entering vi-move mode, or it could
           * be part of a terminal control sequence.  The following
           * logic attempts to disambiguate things in the same
           * fashion as regular vi or readline.
           *
           * When ESC is encountered and there is no other pending
           * character in the pushback queue, then attempt to peek
           * into the input stream (if the feature is enabled) for
           * 150ms. If nothing else is coming, then assume it is
           * not a terminal control sequence, but a raw escape.
           */
          if (c == 27
              && pushBackChar.isEmpty()
              && ((NonBlockingInputStream)reader.getInput()).isNonBlockingEnabled()
              && ((NonBlockingInputStream)reader.getInput()).peek(escapeTimeout) == -2) {
            o = ((KeyMap) o).getAnotherKey();
            if (o == null || o instanceof KeyMap) {
              continue;
            }
            sb.setLength(0);
          }
          else {
            continue;
          }
        }

        /*
         * If we didn't find a binding for the key and there is
         * more than one character accumulated then start checking
         * the largest span of characters from the beginning to
         * see if there is a binding for them.
         *
         * For example if our buffer has ESC,CTRL-M,C the getBound()
         * called previously indicated that there is no binding for
         * this sequence, so this then checks ESC,CTRL-M, and failing
         * that, just ESC. Each keystroke that is pealed off the end
         * during these tests is stuffed onto the pushback buffer so
         * they won't be lost.
         *
         * If there is no binding found, then we go back to waiting for
         * input.
         */
        while ( o == null && sb.length() > 0 ) {
          c = sb.charAt( sb.length() - 1 );
          sb.setLength( sb.length() - 1 );
          Object o2 = reader.getKeys().getBound( sb );
          if ( o2 instanceof KeyMap ) {
            o = ((KeyMap) o2).getAnotherKey();
            if ( o == null ) {
              continue;
            } else {
              pushBackChar.push( (char) c );
            }
          }
        }

        if ( o == null ) {
          continue;
        }

        // It must be that unless it is a macro (...) -> not yet handled
        if (o instanceof Operation) {
          Operation operation = (Operation)o;

          int[] buffer = new int[sb.length()];
          for (int i = 0;i < buffer.length;i++) {
            buffer[i] = sb.codePointAt(i);
          }
          sb.setLength(0);

          //
          console.on(operation, buffer);
        } else {
          System.out.println("No operation: " + o);
        }
      }
      catch (InterruptedIOException e) {
        // Suppress warning for "Interrupted at cycle #0 while waiting for data to become available"
        logger.log(Level.FINE, e.getMessage(), e);
        return;
      }
      catch (IOException e) {
        logger.log(Level.WARNING, e.getMessage(), e);
        return;
      }
    }
  }

  @Override
  public int getWidth() {
    return terminal.getWidth();
  }

  @Override
  public int getHeight() {
    return terminal.getHeight();
  }

  @Override
  public String getProperty(String name) {
    return null;
  }

  @Override
  public boolean takeAlternateBuffer() throws IOException {
    if (ansi) {
      if (!useAlternate) {
        useAlternate = true;

        // To get those codes I captured the output of telnet running top
        // on OSX:
        // 1/ sudo /usr/libexec/telnetd -debug #run telnet
        // 2/ telnet localhost >output.txt
        // 3/ type username + enter
        // 4/ type password + enter
        // 5/ type top + enter
        // 6/ ctrl-c
        // 7/ type exit + enter

        // Save screen and erase
        writer.print("\033[?47h"); // Switches to the alternate screen
        // writer.print("\033[1;43r");
//      processor.writer.print("\033[m"); // Reset to normal (Sets SGR parameters : 0 m == m)
        // writer.print("\033[4l");
        // writer.print("\033[?1h");
        // writer.print("\033[=");
//      processor.writer.print("\033[H"); // Move the cursor to home
//      processor.writer.print("\033[2J"); // Clear screen
//      processor.writer.flush();
      }
      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean releaseAlternateBuffer() throws IOException {
    if (ansi && useAlternate) {
      useAlternate = false;
      writer.print("\033[?47l"); // Switches back to the normal screen
    }
    return true;
  }

  @Override
  public void flush() throws IOException {
    writer.flush();
  }

  @Override
  public void write(CharSequence s) throws IOException {
    write(s, 0, s.length());
  }

  @Override
  public void write(CharSequence s, int start, int end) throws IOException {
    while (start < end) {
      char c = s.charAt(start++);
      write(c);
    }
  }

  @Override
  public void write(char c) throws IOException {
    if (c == '\r') {
      // Skip it
    } else if (c == '\n') {
      writeCRLF();
    } else {
      writer.print(c);
    }
  }

  @Override
  public void write(Style d) throws IOException {
    if (ansi) {
      d.writeAnsiTo(writer);
    }
  }

  @Override
  public void writeDel() throws IOException {
    writer.append("\b \b");
  }

  @Override
  public void writeCRLF() throws IOException {
    writer.append(lineSeparator);
  }

  @Override
  public void cls() throws IOException {
    if (ansi) {
      writer.print("\033[2J");
      writer.print("\033[1;1H");
    }
  }

  @Override
  public boolean moveRight(char c) throws IOException {
    writer.append(c);
    return true;
  }

  @Override
  public boolean moveLeft() throws IOException {
    writer.append("\b");
    return true;
  }

  @Override
  public void close() throws IOException {
    done.countDown();
    reader.shutdown();
  }
}
