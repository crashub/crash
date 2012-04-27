package org.crsh.processor.term;

import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellProcessContext;
import org.crsh.shell.ShellResponse;
import org.crsh.term.TermEvent;

import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class ProcessContext implements ShellProcessContext, Runnable {

  /** . */
  final Processor processor;

  /** . */
  final ShellProcess process;

  ProcessContext(Processor processor, ShellProcess process) {
    this.process = process;
    this.processor = processor;
  }

  public void run() {
    process.execute(this);
  }

  public int getWidth() {
    return processor.term.getWidth();
  }

  public String getProperty(String name) {
    return processor.term.getProperty(name);
  }

  public String readLine(String msg, boolean echo) {
    try {
      processor.term.write(msg);
    }
    catch (IOException e) {
      return null;
    }
    boolean done = false;
    while (true) {
      synchronized (processor.lock) {
        switch (processor.status) {
          case CLOSED:
          case CANCELLING:
            return null;
          case PROCESSING:
            if (processor.queue.size() > 0) {
              TermEvent event = processor.queue.removeFirst();
              if (event instanceof TermEvent.ReadLine) {
                return ((TermEvent.ReadLine)event).getLine().toString();
              }
            }
            break;
          default:
            throw new AssertionError("Does not make sense " + processor.status);
        }
      }
      if (done) {
        return null;
      } else {
        done = true;
        processor.waitingEvent = true;
        try {
          processor.term.setEcho(echo);
          processor.readTerm();
          processor.term.write("\r\n");
        }
        catch (IOException e) {
          processor.log.error("Error when readline line");
        }
        finally {
          processor.waitingEvent = false;
          processor.term.setEcho(true);
        }
      }
    }
  }

  public void end(ShellResponse response) {
    Runnable runnable;
    ProcessContext context;
    Status status;
    synchronized (processor.lock) {

      //
      processor.current = null;
      switch (processor.status) {
        case PROCESSING:
          if (response instanceof ShellResponse.Close) {
            runnable = processor.CLOSE;
            processor.status = Status.CLOSED;
          } else if (response instanceof ShellResponse.Cancelled) {
            runnable = Processor.NOOP;
            processor.status = Status.AVAILABLE;
          } else {
            final String display = response.getText();
            runnable = new Runnable() {
              public void run() {
                processor.write(display);
              }
            };
            processor.status = Status.AVAILABLE;
          }
          break;
        case CANCELLING:
          runnable = Processor.NOOP;
          processor.status = Status.AVAILABLE;
          break;
        default:
          throw new AssertionError("Does not make sense " + processor.status);
      }

      // Do we have a next process to execute ?
      context = processor.peekProcess();
      status = processor.status;
    }

    //
    runnable.run();

    //
    if (context != null) {
      context.run();
    } else if (status == Status.AVAILABLE) {
      processor.writePrompt();
    }
  }
}
