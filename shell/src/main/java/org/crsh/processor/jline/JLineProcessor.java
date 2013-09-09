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

package org.crsh.processor.jline;

import jline.Terminal;
import jline.console.ConsoleReader;
import jline.console.completer.Completer;
import org.crsh.cli.impl.completion.CompletionMatch;
import org.crsh.cli.impl.Delimiter;
import org.crsh.cli.spi.Completion;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

public class JLineProcessor implements Runnable, Completer {

  /** . */
  private final Shell shell;

  /** . */
  final ConsoleReader reader;

  /** . */
  final PrintWriter writer;

  /** . */
  final AtomicReference<ShellProcess> current;

  /** Whether or not we switched on the alternate screen. */
  boolean useAlternate;

  // *********

  private BlockingQueue<Integer> queue;
  private boolean interrupt;
  private Thread pipe;
  volatile private boolean running;
  volatile private boolean eof;
  private InputStream consoleInput;
  private InputStream in;
  private PrintStream out;
  private PrintStream err;
  private Thread thread;
  public static final String IGNORE_INTERRUPTS = "karaf.ignoreInterrupts";

  public JLineProcessor(Shell shell, InputStream in,
                        PrintStream out,
                        PrintStream err,
                        Terminal term) throws IOException {

    //
    this.consoleInput = new ConsoleInputStream();
    this.in = in;
    this.out = out;
    this.err = err;
    this.queue = new ArrayBlockingQueue<Integer>(1024);
    this.pipe = new Thread(new Pipe());
    pipe.setName("gogo shell pipe thread");
    pipe.setDaemon(true);

    //
    ConsoleReader reader = new ConsoleReader(null, consoleInput, out, term);
    reader.addCompleter(this);

    //
    this.shell = shell;
    this.reader = reader;
    this.writer = new PrintWriter(out);
    this.current = new AtomicReference<ShellProcess>();
    this.useAlternate = false;
  }

  private boolean getBoolean(String name) {
    if (name.equals(IGNORE_INTERRUPTS)) {
      return false;
    } else {
      throw new UnsupportedOperationException();
    }
  }

  private void checkInterrupt() throws IOException {
    if (Thread.interrupted() || interrupt) {
      interrupt = false;
      throw new InterruptedIOException("Keyboard interruption");
    }
  }

  private void interrupt() {
//    interrupt = true;
//    thread.interrupt();
    cancel();
  }

  private class ConsoleInputStream extends InputStream
  {
    private int read(boolean wait) throws IOException
    {
      if (!running) {
        return -1;
      }
      checkInterrupt();
      if (eof && queue.isEmpty()) {
        return -1;
      }
      Integer i;
      if (wait) {
        try {
          i = queue.take();
        } catch (InterruptedException e) {
          throw new InterruptedIOException();
        }
        checkInterrupt();
      } else {
        i = queue.poll();
      }
      if (i == null) {
        return -1;
      }
      return i;
    }

    @Override
    public int read() throws IOException
    {
      return read(true);
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException
    {
      if (b == null) {
        throw new NullPointerException();
      } else if (off < 0 || len < 0 || len > b.length - off) {
        throw new IndexOutOfBoundsException();
      } else if (len == 0) {
        return 0;
      }

      int nb = 1;
      int i = read(true);
      if (i < 0) {
        return -1;
      }
      b[off++] = (byte) i;
      while (nb < len) {
        i = read(false);
        if (i < 0) {
          return nb;
        }
        b[off++] = (byte) i;
        nb++;
      }
      return nb;
    }

    @Override
    public int available() throws IOException {
      return queue.size();
    }
  }

  private class Pipe implements Runnable
  {
    public void run()
    {
      try {
        while (running)
        {
          try
          {
            int c = in.read();
            if (c == -1)
            {
              return;
            }
            else if (c == 4 && !getBoolean(IGNORE_INTERRUPTS))
            {
              err.println("^D");
              return;
            }
            else if (c == 3 && !getBoolean(IGNORE_INTERRUPTS))
            {
              err.println("^C");
              reader.getCursorBuffer().clear();
              interrupt();
            }
            queue.put(c);
          }
          catch (Throwable t) {
            return;
          }
        }
      }
      finally
      {
        eof = true;
        try
        {
          queue.put(-1);
        }
        catch (InterruptedException e)
        {
        }
      }
    }
  }

  private String readAndParseCommand() throws IOException {
    String command = null;
    boolean loop = true;
    boolean first = true;
    while (loop) {
      checkInterrupt();
      String line = reader.readLine(first ? getPrompt() : "> ");
      if (line == null)
      {
        break;
      }
      if (command == null) {
        command = line;
      } else {
        command += " " + line;
      }
      if (reader.getHistory().size()==0) {
        reader.getHistory().add(command);
      } else {
        // jline doesn't add blank lines to the history so we don't
        // need to replace the command in jline's console history with
        // an indented one
        if (command.length() > 0 && !" ".equals(command)) {
          reader.getHistory().replace(command);
        }
      }
      try {
        loop = false;
      } catch (Exception e) {
        loop = true;
        first = false;
      }
    }
    return command;
  }

  // *****

  public void run() {
    running = true;
    pipe.start();
    String welcome = shell.getWelcome();
    writer.println(welcome);
    writer.flush();
    loop();
  }

/*
  private String readLine() {
    StringBuilder buffer = new StringBuilder();
    String prompt = getPrompt();
    writer.println();
    writer.flush();
    while (true) {
      try {
        String chunk;
        if ((chunk = reader.readLine(prompt)) == null) {
          return null;
        }
        if (chunk.length() > 0 && chunk.charAt(chunk.length() - 1) == '\\') {
          prompt = "> ";
          buffer.append(chunk, 0, chunk.length() - 1);
        } else {
          buffer.append(chunk);
          return buffer.toString();
        }
      }
      catch (IOException e) {
        // What should we do other than that ?
        return null;
      }
    }
  }
*/

  private void loop() {
    while (true) {

      //
      String line = null;
      try {
        line = readAndParseCommand();
      }
      catch (Throwable t) {
        t.printStackTrace();
      }

      //
      ShellProcess process = shell.createProcess(line);
      JLineProcessContext context = new JLineProcessContext(this);
      current.set(process);
      try {
        process.execute(context);
        try {
          context.latch.await();
        }
        catch (InterruptedException ignore) {
          // At the moment
        }
      }
      finally {
        current.set(null);
      }

      //
      ShellResponse response = context.resp.get();

      // Write message
      boolean flushed = false;
      String msg = response.getMessage();
      if (msg.length() > 0) {
        writer.write(msg);
        writer.flush();
        flushed = true;
      }

      //
      if (response instanceof ShellResponse.Cancelled) {
        // Do nothing
      } else if (response instanceof ShellResponse.Close) {
        break;
      } else {
        if (!flushed) {
          writer.flush();
        }
      }
    }
  }

  public int complete(String buffer, int cursor, List<CharSequence> candidates) {
    String prefix = buffer.substring(0, cursor);
    CompletionMatch completion = shell.complete(prefix);
    Completion vc = completion.getValue();
    if (vc.isEmpty()) {
      return -1;
    }
    Delimiter delimiter = completion.getDelimiter();
    for (Map.Entry<String, Boolean> entry : vc) {
      StringBuilder sb = new StringBuilder();
      sb.append(vc.getPrefix());
      try {
        delimiter.escape(entry.getKey(), sb);
        if (entry.getValue()) {
          sb.append(completion.getDelimiter().getValue());
        }
        candidates.add(sb.toString());
      }
      catch (IOException ignore) {
      }
    }
    return cursor - vc.getPrefix().length();
  }

  public void cancel() {
    ShellProcess process = current.get();
    if (process != null) {
      process.cancel();
    } else {
      // Do nothing
    }
  }

  String getPrompt() {
    String prompt = shell.getPrompt();
    return prompt == null ? "% " : prompt;
  }
}
