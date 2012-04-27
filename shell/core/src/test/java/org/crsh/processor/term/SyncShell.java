package org.crsh.processor.term;

import org.crsh.AbstractTestCase;
import org.crsh.cmdline.CommandCompletion;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellProcessContext;
import org.crsh.shell.ShellResponse;

import java.util.LinkedList;
import java.util.concurrent.Callable;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class SyncShell implements Shell {

  /** . */
  private final LinkedList<Throwable> failures;

  /** . */
  private final Object lock;

  /** . */
  private final LinkedList<ShellProcess> queue;

  public SyncShell() {
    this.lock = new Object();
    this.queue = new LinkedList<ShellProcess>();
    this.failures = new LinkedList<Throwable>();
  }

  public <R extends ShellResponse> void publish(final Callable<R> callable) {
    publish(new ShellProcess() {
      public void execute(ShellProcessContext processContext) {
        try {
          R response = callable.call();
          processContext.end(response);
        }
        catch (Exception e) {
          throw AbstractTestCase.failure(e);
        }
      }
      public void cancel() {
      }
    });
  }

  public void publish(ShellProcess process) {
    synchronized (lock) {
      queue.add(process);
      lock.notifyAll();
    }
  }

  public String getWelcome() {
    return "welcome";
  }

  public String getPrompt() {
    return "%";
  }

  public ShellProcess createProcess(String request) throws IllegalStateException {
    synchronized (lock) {
      while (true) {
        if (queue.size() > 0) {
          return queue.removeFirst();
        } else {
          try {
            lock.wait();
          }
          catch (InterruptedException e) {
            throw AbstractTestCase.failure(e);
          }
        }
      }
    }
  }

  public CommandCompletion complete(String prefix) {
    throw new UnsupportedOperationException();
  }
}
