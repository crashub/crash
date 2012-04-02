package org.crsh;

import junit.framework.AssertionFailedError;

import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class CommandQueue implements Executor {

  /** . */
  private final LinkedList<Runnable> queue = new LinkedList<Runnable>();

  public synchronized void execute(Runnable command) {
    queue.addLast(command);
  }

  public synchronized int getSize() {
    return queue.size();
  }

  public synchronized Future<Runnable> executeAsync() {
    if (queue.size() == 0) {
      throw new AssertionFailedError();
    }
    Runnable runnable = queue.removeFirst();
    FutureTask<Runnable> future = new FutureTask<Runnable>(runnable, runnable);
    new Thread(future).start();
    return future;
  }
}
