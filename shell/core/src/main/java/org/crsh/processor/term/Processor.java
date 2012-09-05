package org.crsh.processor.term;

import org.crsh.cmdline.CommandCompletion;
import org.crsh.cmdline.Delimiter;
import org.crsh.cmdline.spi.ValueCompletion;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellProcess;
import org.crsh.text.ChunkSequence;
import org.crsh.term.Term;
import org.crsh.term.TermEvent;
import org.crsh.util.CloseableList;
import org.crsh.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public final class Processor implements Runnable {

  /** . */
  static final Runnable NOOP = new Runnable() {
    public void run() {
    }
  };

  /** . */
  final Runnable WRITE_PROMPT = new Runnable() {
    public void run() {
      writePrompt();
    }
  };

  /** . */
  final Runnable CLOSE = new Runnable() {
    public void run() {
      close();
    }
  };

  /** . */
  private final Runnable READ_TERM = new Runnable() {
    public void run() {
      readTerm();
    }
  };

  /** . */
  final Logger log = LoggerFactory.getLogger(Processor.class);

  /** . */
  final Term term;

  /** . */
  final Shell shell;

  /** . */
  final LinkedList<TermEvent> queue;

  /** . */
  final Object lock;

  /** . */
  ProcessContext current;

  /** . */
  Status status;

  /** A flag useful for unit testing to know when the thread is reading. */
  volatile boolean waitingEvent;

  /** . */
  private final CloseableList listeners;

  public Processor(Term term, Shell shell) {
    this.term = term;
    this.shell = shell;
    this.queue = new LinkedList<TermEvent>();
    this.lock = new Object();
    this.status = Status.AVAILABLE;
    this.listeners = new CloseableList();
    this.waitingEvent = false;
  }

  public boolean isWaitingEvent() {
    return waitingEvent;
  }

  public void run() {


    // Display initial stuff
    try {
      String welcome = shell.getWelcome();
      log.debug("Writing welcome message to term");
      term.write(new ChunkSequence(welcome));
      log.debug("Wrote welcome message to term");
      writePrompt();
    }
    catch (IOException e) {
      e.printStackTrace();
    }

    //
    while (true) {
      try {
        if (!iterate()) {
          break;
        }
      }
      catch (IOException e) {
        e.printStackTrace();
      }
      catch (InterruptedException e) {
        break;
      }
    }
  }

  boolean iterate() throws InterruptedException, IOException {

    //
    Runnable runnable;
    synchronized (lock) {
      switch (status) {
        case AVAILABLE:
          runnable =  peekProcess();
          if (runnable != null) {
            break;
          }
        case PROCESSING:
        case CANCELLING:
          runnable = READ_TERM;
          break;
        case CLOSED:
          return false;
        default:
          throw new AssertionError();
      }
    }

    //
    runnable.run();

    //
    return true;
  }

  // We assume this is called under lock synchronization
  ProcessContext peekProcess() {
    while (true) {
      synchronized (lock) {
        if (status == Status.AVAILABLE) {
          if (queue.size() > 0) {
            TermEvent event = queue.removeFirst();
            if (event instanceof TermEvent.Complete) {
              complete(((TermEvent.Complete)event).getLine());
            } else {
              String line = ((TermEvent.ReadLine)event).getLine().toString();
              if (line.length() > 0) {
                term.addToHistory(line);
              }
              ShellProcess process = shell.createProcess(line);
              current =  new ProcessContext(this, process);
              status = Status.PROCESSING;
              return current;
            }
          } else {
            break;
          }
        } else {
          break;
        }
      }
    }
    return null;
  }

  /** . */
  private final Object termLock = new Object();

  private boolean reading = false;

  void readTerm() {

    //
    synchronized (termLock) {
      if (reading) {
        try {
          termLock.wait();
          return;
        }
        catch (InterruptedException e) {
          throw new AssertionError(e);
        }
      } else {
        reading = true;
      }
    }

    //
    try {
      TermEvent event = term.read();

      //
      Runnable runnable;
      if (event instanceof TermEvent.Break) {
        synchronized (lock) {
          queue.clear();
          if (status == Status.PROCESSING) {
            status = Status.CANCELLING;
            runnable = new Runnable() {
              ProcessContext context = current;
              public void run() {
                context.process.cancel();
              }
            };
          }
          else if (status == Status.AVAILABLE) {
            runnable = WRITE_PROMPT;
          } else {
            runnable = NOOP;
          }
        }
      } else if (event instanceof TermEvent.Close) {
        synchronized (lock) {
          queue.clear();
          if (status == Status.PROCESSING) {
            runnable = new Runnable() {
              ProcessContext context = current;
              public void run() {
                context.process.cancel();
                close();
              }
            };
          } else if (status != Status.CLOSED) {
            runnable = CLOSE;
          } else {
            runnable = NOOP;
          }
          status = Status.CLOSED;
        }
      } else {
        synchronized (queue) {
          queue.addLast(event);
          runnable = NOOP;
        }
      }

      //
      runnable.run();
    }
    catch (IOException e) {
      log.error("Error when reading term", e);
    }
    finally {
      synchronized (termLock) {
        reading = false;
        termLock.notifyAll();
      }
    }
  }

  void close() {
    listeners.close();
  }

  public void addListener(Closeable listener) {
    listeners.add(listener);
  }

  void write(ChunkSequence reader) {
    try {
      term.write(reader);
    }
    catch (IOException e) {
      log.error("Write to term failure", e);
    }
  }

  void writePrompt() {
    String prompt = shell.getPrompt();
    try {
      String p = prompt == null ? "% " : prompt;
      ChunkSequence cr = new ChunkSequence().append("\r\n").append(p);
      CharSequence buffer = term.getBuffer();
      if (buffer != null) {
        cr.append(buffer);
      }
      term.write(cr);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void complete(CharSequence prefix) {
    log.debug("About to get completions for " + prefix);
    CommandCompletion completion = shell.complete(prefix.toString());
    ValueCompletion completions = completion.getValue();
    log.debug("Completions for " + prefix + " are " + completions);

    //
    Delimiter delimiter = completion.getDelimiter();

    try {
      // Try to find the greatest prefix among all the results
      if (completions.getSize() == 0) {
        // Do nothing
      } else if (completions.getSize() == 1) {
        Map.Entry<String, Boolean> entry = completions.iterator().next();
        Appendable buffer = term.getInsertBuffer();
        String insert = entry.getKey();
        delimiter.escape(insert, term.getInsertBuffer());
        if (entry.getValue()) {
          buffer.append(completion.getDelimiter().getValue());
        }
      } else {
        String commonCompletion = Strings.findLongestCommonPrefix(completions.getSuffixes());
        if (commonCompletion.length() > 0) {
          delimiter.escape(commonCompletion, term.getInsertBuffer());
        } else {
          // Format stuff
          int width = term.getWidth();

          //
          String completionPrefix = completions.getPrefix();

          // Get the max length
          int max = 0;
          for (String suffix : completions.getSuffixes()) {
            max = Math.max(max, completionPrefix.length() + suffix.length());
          }

          // Separator : use two whitespace like in BASH
          max += 2;

          //
          StringBuilder sb = new StringBuilder().append('\n');
          if (max < width) {
            int columns = width / max;
            int index = 0;
            for (String suffix : completions.getSuffixes()) {
              sb.append(completionPrefix).append(suffix);
              for (int l = completionPrefix.length() + suffix.length();l < max;l++) {
                sb.append(' ');
              }
              if (++index >= columns) {
                index = 0;
                sb.append('\n');
              }
            }
            if (index > 0) {
              sb.append('\n');
            }
          } else {
            for (Iterator<String> i = completions.getSuffixes().iterator();i.hasNext();) {
              String suffix = i.next();
              sb.append(commonCompletion).append(suffix);
              if (i.hasNext()) {
                sb.append('\n');
              }
            }
            sb.append('\n');
          }

          // We propose
          term.write(new ChunkSequence(sb.toString()));
          writePrompt();
        }
      }
    }
    catch (IOException e) {
      log.error("Could not write completion", e);
    }
  }
}
