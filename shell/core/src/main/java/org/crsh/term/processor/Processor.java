/*
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.crsh.term.processor;

import org.crsh.shell.Shell;
import org.crsh.shell.ShellProcess;
import org.crsh.term.Term;
import org.crsh.term.TermEvent;
import org.crsh.util.FutureListener;
import org.crsh.util.LatchedFuture;
import org.crsh.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * <p>The processor is the glue between a term object and a shell object. It mainly read the input from the
 * term and executes shell commands.</p>
 * 
 * <p>The class implements the {@link Runnable} interface to perform its processing.</p>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public final class Processor implements Runnable {

  /** . */
  final Logger log = LoggerFactory.getLogger(Processor.class);

  /** . */
  final Term term;

  /** . */
  private final AtomicReference<Status> status;

  /** . */
  private final Shell shell;

  /** The current process being executed. */
  volatile ShellProcess process;

  /** . */
  private final List<Closeable> listeners;

  public Processor(Term term, Shell shell) {
    this.term = term;
    this.status = new AtomicReference<Status>(State.INITIAL.available);
    this.shell = shell;
    this.process = null;
    this.listeners = new ArrayList<Closeable>();
  }

  public void run() {
    while (true) {
      Result result = execute();
      State state = result.getState();
      if (state == State.CLOSED) {
        break;
      }
    }
  }

  public boolean isAvailable() {
    return process == null;
  }

  public State getState() {
    return status.get().getState();
  }

  public Result execute() {

    //
    final Status _status = status.get();

    Task task = null;

    if (_status == State.INITIAL.available) {

      task = new Task() {
        @Override
        protected LatchedFuture<State> execute() {
          try {
            String welcome = shell.getWelcome();
            log.debug("Writing welcome message to term");
            term.write(welcome);
            log.debug("Wrote welcome message to term");
            writePrompt();
          }
          catch (IOException e) {
            e.printStackTrace();
          }
          return new LatchedFuture<State>(State.OPEN);
        }
      };

    } else if (_status == State.OPEN.available) {

      task = new Task() {
        @Override
        protected LatchedFuture<State> execute() {
          //
          TermEvent event = null;

          try {
            log.debug("About to read next term event");
            event = term.read();
            log.debug("Read next term event " + event);
          } catch (IOException e) {
            if (status.get() == State.OPEN.available) {
              log.error("Could not read term data", e);
            } else {
              log.debug("Exception but term is considered as closed", e);
              // We continue it will lead to getting out of the loop
            }
          }

          //
          if (event == null) {
            // Do nothing wait until next event
            return new LatchedFuture<State>(State.OPEN);
          } else if (event instanceof TermEvent.ReadLine) {
            String line = ((TermEvent.ReadLine)event).getLine().toString();
            log.debug("Submitting command " + line);

            //
            ShellInvoker invoker = new ShellInvoker(Processor.this);

            // Process
            process = shell.createProcess(((TermEvent.ReadLine) event).getLine().toString());

            //
            process.execute(invoker);

            //
            if (line.length() > 0) {
              term.addToHistory(line);
            }

            //
            return invoker.result;
          } else if (event instanceof TermEvent.Break) {
            if (process != null) {
              process.cancel();
            } else {
              log.debug("Ignoring action " + event);
              writePrompt();
            }
            return new LatchedFuture<State>(State.OPEN);
          } else if (event instanceof TermEvent.Complete) {
            TermEvent.Complete complete = (TermEvent.Complete)event;
            String prefix = complete.getLine().toString();
            log.debug("About to get completions for " + prefix);
            Map<String, String> completions = shell.complete(prefix);
            log.debug("Completions for " + prefix + " are " + completions);

            // Try to find the greatest prefix among all the results
            String commonCompletion;
            if (completions.size() == 0) {
              commonCompletion = "";
            } else if (completions.size() == 1) {
              Map.Entry<String, String> entry = completions.entrySet().iterator().next();
              commonCompletion = entry.getKey() + entry.getValue();
            } else {
              commonCompletion = Strings.findLongestCommonPrefix(completions.keySet());
            }

            //
            if (commonCompletion.length() > 0) {
              try {
                term.bufferInsert(commonCompletion);
              }
              catch (IOException e) {
                e.printStackTrace();
              }
            } else {
              if (completions.size() > 1) {
                // We propose
                StringBuilder sb = new StringBuilder("\n");
                for (Iterator<String> i = completions.keySet().iterator();i.hasNext();) {
                  String completion = i.next();
                  sb.append(completion);
                  if (i.hasNext()) {
                    sb.append(" ");
                  }
                }
                sb.append("\n");
                try {
                  term.write(sb.toString());
                }
                catch (IOException e) {
                  e.printStackTrace();
                }
                writePrompt();
              }
            }
            return new LatchedFuture<State>(State.OPEN);
          } else if (event instanceof TermEvent.Close) {
            return new LatchedFuture<State>(State.WANT_CLOSE);
          } else {
            return new LatchedFuture<State>(State.OPEN);
          }
        }
      };
    } else if (_status == State.WANT_CLOSE.available) {

      task = new Task() {
        @Override
        protected LatchedFuture<State> execute() {
          //
          log.debug("Closing processor");

          // Make a copy
          ArrayList<Closeable> listeners;
          synchronized (Processor.this.listeners) {
            listeners = new ArrayList<Closeable>(Processor.this.listeners);
          }

          // Status to closed, we won't process any further request
          // it's important to set the status before closing anything to
          // avoid a race condition because closing a listener may
          // cause an interrupted exception
          status.set(State.CLOSED.available);

          //
          for (Closeable listener : listeners) {
            try {
              log.debug("Closing " + listener.getClass().getSimpleName());
              listener.close();
            }
            catch (Exception e) {
              e.printStackTrace();
            }
          }

          //
          return new LatchedFuture<State>(State.CLOSED);
        }
      };
    } else {
      //
    }

    //
    if (task != null) {

      // Add listener to update our state
      final LatchedFuture<State> futureState = task.execute();

      // It will update the status
      futureState.addListener(new FutureListener<State>() {
        public void completed(State value) {
          status.set(value.available);
        }
      });

      //
      return new Result() {
        public State getState() {
          try {
            return futureState.get();
          } catch (InterruptedException e) {
            return status.get().getState();
          } catch (ExecutionException e) {
            return status.get().getState();
          }
        }
      };
    } else {
      return new Result() {
        public State getState() {
          return _status.getState();
        }
      };
    }
  }

  void writePrompt() {
    String prompt = shell.getPrompt();
    try {
      String p = prompt == null ? "% " : prompt;
      term.write("\r\n");
      term.write(p);
      term.write(term.getBuffer());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void addListener(Closeable listener) {
    if (listener == null) {
      throw new NullPointerException();
    }
    synchronized (listeners) {
      if (listeners.contains(listener)) {
        throw new IllegalStateException("Already listening");
      }
      listeners.add(listener);
    }
  }
}
