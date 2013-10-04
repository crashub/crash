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
import jline.console.UserInterruptException;
import jline.console.completer.Completer;
import org.crsh.cli.impl.completion.CompletionMatch;
import org.crsh.cli.impl.Delimiter;
import org.crsh.cli.spi.Completion;
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

  /** Whether or not we switched on the alternate screen. */
  boolean useAlternate;

  public JLineProcessor(Shell shell, ConsoleReader reader, PrintWriter writer) {
    this.shell = shell;
    this.reader = reader;
    this.writer = writer;
    this.current = new AtomicReference<ShellProcess>();
    this.useAlternate = false;
  }

  public void run() {
    String welcome = shell.getWelcome();
    writer.println(welcome);
    writer.flush();
    loop();
  }

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

  private void loop() {
    while (true) {
      String line;
      try {
        line = readLine();
      } catch (UserInterruptException e) {
        continue;
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
