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

package org.crsh.telnet.term.processor;

import org.crsh.cli.impl.completion.CompletionMatch;
import org.crsh.cli.impl.line.LineParser;
import org.crsh.cli.impl.line.MultiLineVisitor;
import org.crsh.cli.spi.Completion;
import org.crsh.cli.impl.Delimiter;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellProcess;
import org.crsh.telnet.term.Term;
import org.crsh.telnet.term.TermEvent;
import org.crsh.text.Screenable;
import org.crsh.text.ScreenContext;
import org.crsh.text.Style;
import org.crsh.util.CloseableList;
import org.crsh.util.Utils;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Processor implements Runnable, ScreenContext {

  /** . */
  private static final String CONTINUE_PROMPT = "> ";

  /** . */
  static final Runnable NOOP = new Runnable() {
    public void run() {
    }
  };

  /** . */
  final Runnable WRITE_PROMPT_TASK = new Runnable() {
    public void run() {
      writePromptFlush();
    }
  };

  /** . */
  final Runnable CLOSE_TASK = new Runnable() {
    public void run() {
      close();
    }
  };

  /** . */
  private final Runnable READ_TERM_TASK = new Runnable() {
    public void run() {
      readTerm();
    }
  };

  /** . */
  final Logger log = Logger.getLogger(Processor.class.getName());

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

  /** . */
  private final LineParser lineBuffer;

  /** . */
  private final MultiLineVisitor lineVisitor;

  public Processor(Term term, Shell shell) {
    this.term = term;
    this.shell = shell;
    this.queue = new LinkedList<TermEvent>();
    this.lock = new Object();
    this.status = Status.AVAILABLE;
    this.listeners = new CloseableList();
    this.waitingEvent = false;
    this.lineVisitor = new MultiLineVisitor();
    this.lineBuffer = new LineParser(lineVisitor);
  }

  public boolean isWaitingEvent() {
    return waitingEvent;
  }

  public void run() {


    // Display initial stuff
    try {
      String welcome = shell.getWelcome();
      log.log(Level.FINE, "Writing welcome message to term");
      term.append(welcome);
      log.log(Level.FINE, "Wrote welcome message to term");
      writePromptFlush();
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
        Thread.currentThread().interrupt();
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
          runnable = READ_TERM_TASK;
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
              lineBuffer.append(line);
              if (!lineBuffer.crlf()) {
                try {
                  term.append(CONTINUE_PROMPT);
                  term.flush();
                }
                catch (IOException e) {
                  e.printStackTrace();
                }
              } else {
                String command = lineVisitor.getRaw();
                lineBuffer.reset();
                if (command.length() > 0) {
                  term.addToHistory(command);
                }
                ShellProcess process = shell.createProcess(command);
                current =  new ProcessContext(this, process);
                status = Status.PROCESSING;
                return current;
              }
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

  /** . */
  private boolean termReading = false;

  void readTerm() {

    //
    synchronized (termLock) {
      if (termReading) {
        try {
          termLock.wait();
          return;
        }
        catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new AssertionError(e);
        }
      } else {
        termReading = true;
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
            runnable = WRITE_PROMPT_TASK;
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
            runnable = CLOSE_TASK;
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
      log.log(Level.SEVERE, "Error when reading term", e);
    }
    finally {
      synchronized (termLock) {
        termReading = false;
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

  @Override
  public int getWidth() {
    return term.getWidth();
  }

  @Override
  public int getHeight() {
    return term.getHeight();
  }

  @Override
  public Appendable append(char c) throws IOException {
    term.append(c);
    return this;
  }

  @Override
  public Appendable append(CharSequence s) throws IOException {
    term.append(s);
    return this;
  }

  @Override
  public Appendable append(CharSequence csq, int start, int end) throws IOException {
    term.append(csq, start, end);
    return this;
  }

  @Override
  public Screenable append(Style style) throws IOException {
    term.append(style);
    return this;
  }

  @Override
  public Screenable cls() throws IOException {
    term.cls();
    return this;
  }

  public void flush() throws IOException {
    throw new UnsupportedOperationException("what does it mean?");
  }

  void writePromptFlush() {
    String prompt = shell.getPrompt();
    try {
      StringBuilder sb = new StringBuilder("\r\n");
      String p = prompt == null ? "% " : prompt;
      sb.append(p);
      CharSequence buffer = term.getBuffer();
      if (buffer != null) {
        sb.append(buffer);
      }
      term.append(sb);
      term.flush();
    } catch (IOException e) {
      // Todo : improve that
      e.printStackTrace();
    }
  }

  private void complete(CharSequence prefix) {
    log.log(Level.FINE, "About to get completions for " + prefix);
    CompletionMatch completion = shell.complete(prefix.toString());
    Completion completions = completion.getValue();
    log.log(Level.FINE, "Completions for " + prefix + " are " + completions);

    //
    Delimiter delimiter = completion.getDelimiter();

    try {
      // Try to find the greatest prefix among all the results
      if (completions.getSize() == 0) {
        // Do nothing
      } else if (completions.getSize() == 1) {
        Map.Entry<String, Boolean> entry = completions.iterator().next();
        Appendable buffer = term.getDirectBuffer();
        String insert = entry.getKey();
        term.getDirectBuffer().append(delimiter.escape(insert));
        if (entry.getValue()) {
          buffer.append(completion.getDelimiter().getValue());
        }
      } else {
        String commonCompletion = Utils.findLongestCommonPrefix(completions.getValues());

        // Format stuff
        int width = term.getWidth();

        //
        String completionPrefix = completions.getPrefix();

        // Get the max length
        int max = 0;
        for (String suffix : completions.getValues()) {
          max = Math.max(max, completionPrefix.length() + suffix.length());
        }

        // Separator : use two whitespace like in BASH
        max += 2;

        //
        StringBuilder sb = new StringBuilder().append('\n');
        if (max < width) {
          int columns = width / max;
          int index = 0;
          for (String suffix : completions.getValues()) {
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
          for (Iterator<String> i = completions.getValues().iterator();i.hasNext();) {
            String suffix = i.next();
            sb.append(commonCompletion).append(suffix);
            if (i.hasNext()) {
              sb.append('\n');
            }
          }
          sb.append('\n');
        }

        // We propose
        term.append(sb);

        // Rewrite prompt
        writePromptFlush();

        // If we have common completion we append it now
        if (commonCompletion.length() > 0) {
          term.getDirectBuffer().append(delimiter.escape(commonCompletion));
        }
      }
    }
    catch (IOException e) {
      log.log(Level.SEVERE, "Could not write completion", e);
    }
  }
}
