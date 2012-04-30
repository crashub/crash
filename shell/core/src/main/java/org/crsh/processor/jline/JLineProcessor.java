package org.crsh.processor.jline;

import jline.console.ConsoleReader;
import jline.console.completer.Completer;
import org.crsh.cmdline.CommandCompletion;
import org.crsh.cmdline.Delimiter;
import org.crsh.cmdline.spi.ValueCompletion;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellResponse;
import org.crsh.term.ANSIFontBuilder;
import org.crsh.term.DataFragment;
import org.crsh.term.FormattingData;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class JLineProcessor implements Runnable, Completer {

  /** . */
  private final Shell shell;

  /** . */
  final ConsoleReader reader;

  /** . */
  final PrintWriter writer;

  /** . */
  final AtomicReference<ShellProcess> current;

  /** . */
  private final ANSIFontBuilder ansiBuilder;

  public JLineProcessor(Shell shell, ConsoleReader reader, PrintWriter writer) {
    this.shell = shell;
    this.reader = reader;
    this.writer = writer;
    this.current = new AtomicReference<ShellProcess>();
    this.ansiBuilder = new ANSIFontBuilder();
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
        for (DataFragment f : response.getData()) {
          if (f instanceof FormattingData) {
            writer.print(ansiBuilder.build((FormattingData) f));
          } else {
            writer.print(f.get());
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
