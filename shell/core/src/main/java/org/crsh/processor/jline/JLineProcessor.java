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

import jline.console.ConsoleReader;
import jline.console.completer.Completer;
import org.crsh.cmdline.CommandCompletion;
import org.crsh.cmdline.Delimiter;
import org.crsh.cmdline.spi.ValueCompletion;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
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

  public JLineProcessor(Shell shell, ConsoleReader reader, PrintWriter writer) {
    this.shell = shell;
    this.reader = reader;
    this.writer = writer;
    this.current = new AtomicReference<ShellProcess>();
  }

  public void run() {

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
    writer.print("\033[m"); // Reset to normal (Sets SGR parameters : 0 m == m)
    // writer.print("\033[4l");
    // writer.print("\033[?1h");
    // writer.print("\033[=");
    writer.print("\033[H"); // Move the cursor to home
    writer.print("\033[2J"); // Clear screen
    writer.flush();

    //
    try {
      //
      String welcome = shell.getWelcome();
      writer.println(welcome);
      writer.flush();

      //
      loop();
    }
    finally {
      // Restore screen
      writer.print("\033[?47L"); // Switches back to the normal screen
      writer.flush();
    }
  }

  private void loop() {
    while (true) {
      String prompt = getPrompt();
      String line;
      try {
        writer.println();
        writer.flush();
        if ((line = reader.readLine(prompt)) == null) {
          break;
        }
      }
      catch (IOException e) {
        // What should we do other than that ?
        break;
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
    CommandCompletion completion = shell.complete(prefix);
    ValueCompletion vc = completion.getValue();
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
