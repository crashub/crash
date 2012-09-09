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
import org.crsh.text.Chunk;
import org.crsh.text.Style;
import org.crsh.text.TextChunk;

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

    //
    String welcome = shell.getWelcome();
    writer.println(welcome);
    writer.flush();

    //
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
      ShellResponse response = context.resp.get();

      //
      if (response instanceof ShellResponse.Cancelled) {
        // Do nothing
      } else if (response instanceof ShellResponse.Close) {
        break;
      } else {

        for (Chunk chunk : response.getReader()) {
          if (chunk instanceof TextChunk) {
            TextChunk textChunk = (TextChunk)chunk;
            if (textChunk.getText() != null) {
              writer.append(textChunk.getText());
            }
          } else if (chunk instanceof Style) {
            try {
              ((Style)chunk).writeAnsiTo(writer);
            }
            catch (IOException ignore) {
            }
          } else {
            try {
              reader.clearScreen();
            }
            catch (IOException ignore) {
            }
          }
        }
        writer.flush();
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
      writer.println();
      writer.print(getPrompt());
      writer.flush();
    }
  }

  String getPrompt() {
    String prompt = shell.getPrompt();
    return prompt == null ? "% " : prompt;
  }
}
