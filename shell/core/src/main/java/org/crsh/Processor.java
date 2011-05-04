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

package org.crsh;

import org.crsh.shell.Shell;
import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellProcessContext;
import org.crsh.shell.ShellResponse;
import org.crsh.term.Term;
import org.crsh.term.TermEvent;
import org.crsh.util.FutureListener;
import org.crsh.util.LatchedFuture;
import org.crsh.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Processor implements Runnable {

  public static enum State {

    INITIAL,

    OPEN,

    WANT_CLOSE,

    CLOSED;

    /** . */
    public final Status available;

    /** . */
    public final Status busy;

    State() {
      this.available = new Status(this, true);
      this.busy = new Status(this, false);
    }
  }

  public static class Status {

    /** . */
    private final State state;

    /** . */
    private final boolean available;

    private Status(State state, boolean available) {
      this.state = state;
      this.available = available;
    }

    public State getState() {
      return state;
    }

    public boolean isAvailable() {
      return available;
    }

    public boolean isBusy() {
      return !available;
    }

    @Override
    public boolean equals(Object o) {
      if (o == this) {
        return true;
      } else if (o instanceof Status) {
        Status that = (Status)o;
        return state == that.state && available == that.available;
      } else {
        return false;
      }
    }
  }

  public interface Result {

    State getState();

  }

  /** . */
  private final Logger log = LoggerFactory.getLogger(Processor.class);

  /** . */
  private final Term term;

  /** . */
  private final AtomicReference<Status> status;

  /** . */
  private final Shell shell;

  /** The current process being executed. */
  private volatile ShellProcess process;

  /** . */
  private final List<ProcessorListener> listeners;

  public Processor(Term term, Shell shell) {
    this.term = term;
    this.status = new AtomicReference<Status>(State.INITIAL.available);
    this.shell = shell;
    this.process = null;
    this.listeners = new ArrayList<ProcessorListener>();
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

  private static abstract class Task {

    protected abstract LatchedFuture<State> execute();

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
            ShellInvoker invoker = new ShellInvoker();

            // Process
            shell.process(((TermEvent.ReadLine)event).getLine().toString(), invoker);

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
          ArrayList<ProcessorListener> listeners;
          synchronized (Processor.this.listeners) {
            listeners = new ArrayList<ProcessorListener>(Processor.this.listeners);
          }

          // Status to closed, we won't process any further request
          // it's important to set the status before closing anything to
          // avoid a race condition because closing a listener may
          // cause an interrupted exception
          status.set(State.CLOSED.available);

          //
          for (ProcessorListener listener : listeners) {
            try {
              log.debug("Closing " + listener.getClass().getSimpleName());
              listener.closed();
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

  private void writePrompt() {
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

  private class ShellInvoker implements ShellProcessContext {

    /** . */
    private final LatchedFuture<State> result = new LatchedFuture<State>();

    public int getWidth() {
      return term.getWidth();
    }

    public void begin(ShellProcess process) {
      Processor.this.process = process;
    }

    public String readLine(String msg, boolean echo) {
      try {
        term.setEcho(echo);
        term.write(msg);
        TermEvent action = term.read();
        CharSequence line = null;
        if (action instanceof TermEvent.ReadLine) {
          line = ((TermEvent.ReadLine)action).getLine();
          log.debug("Read from console");
        }
        else {
          log.debug("Ignoring action " + action + " returning null");
        }
        term.write("\r\n");
        return line.toString();
      }
      catch (Exception e) {
        log.error("Reading from console failed", e);
        return null;
      }
      finally {
        term.setEcho(true);
      }
    }

    public void end(ShellResponse response) {
      try {

        //
        if (response instanceof ShellResponse.Close) {
          System.out.println("received close response");
          result.set(State.WANT_CLOSE);
        } else {
          if (response instanceof ShellResponse.Cancelled) {
            result.set(State.OPEN);
          } else {
            String ret = response.getText();
            log.debug("Command completed with result " + ret);
            try {
              term.write(ret);
            }
            catch (IOException e) {
              log.error("Write to term failure", e);
            }
            process = null;
          }

          //
          writePrompt();

          //
          result.set(State.OPEN);
        }
      }
      finally {
        process = null;
      }
    }
  }

  public void addListener(ProcessorListener listener) {
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
