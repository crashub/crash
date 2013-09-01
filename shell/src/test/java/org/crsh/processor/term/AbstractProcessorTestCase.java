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

package org.crsh.processor.term;

import org.crsh.AbstractTestCase;
import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellProcessContext;
import org.crsh.shell.ShellResponse;
import org.crsh.term.TermEvent;
import org.crsh.text.Text;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractProcessorTestCase extends AbstractTestCase {

  /** . */
  protected SyncTerm term;

  /** . */
  protected SyncShell shell;

  /** . */
  protected Processor processor;

  /** . */
  protected Thread thread;

  @Override
  protected void setUp() throws Exception {
    SyncTerm term = createTerm();
    SyncShell shell = createShell();
    Processor processor = createProcessor(term, shell);

    this.term = term;
    this.shell = shell;
    this.processor = processor;
    this.thread = new Thread(processor);

    //
    thread.start();
  }

  protected abstract SyncTerm createTerm();

  protected abstract SyncShell createShell();

  protected abstract Processor createProcessor(SyncTerm term, SyncShell shell);

  public void testTermClose() throws Exception {
    final AtomicBoolean closed = new AtomicBoolean();
    processor.addListener(new Closeable() {
      public void close() throws IOException {
        closed.set(true);
      }
    });
    term.publish(TermEvent.close());
    assertJoin(thread);
    assertTrue(closed.get());
  }

  public void testBufferEvent() throws Exception {
    final CyclicBarrier syncA = new CyclicBarrier(2);
    final CountDownLatch syncB = new CountDownLatch(1);
    term.publish(TermEvent.readLine("foo"));
    shell.publish(new ShellRunnable() {
      public void run(ShellProcessContext context) throws Exception {
        syncA.await();
        syncB.await();
        context.write(Text.create("foo"));
        context.end(ShellResponse.ok());
      }
    });
    syncA.await();
    term.publish(TermEvent.readLine("bar"));
    syncB.countDown();
    shell.publish(new ShellRunnable() {
      public void run(ShellProcessContext context) throws Exception {
        context.write(Text.create("bar"));
        context.end(ShellResponse.ok());
      }
    });
    term.publish(TermEvent.close());
    assertJoin(thread);
  }

  protected abstract int getBarrierSize();

  public void testCancellation() throws Exception {
    final CyclicBarrier syncA = new CyclicBarrier(2);
    final CyclicBarrier syncB = new CyclicBarrier(getBarrierSize());
    term.publish(TermEvent.readLine("foo"));
    shell.publish(new ShellProcess() {
      public void execute(ShellProcessContext processContext) {
        try {
          syncA.await();
          syncB.await();
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }
      public void cancel() {
        try {
          syncB.await();
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
    syncA.await();
    term.publish(TermEvent.brk());
    term.publish(TermEvent.readLine("bar"));
    shell.publish(new ShellRunnable() {
      public void run(ShellProcessContext context) throws Exception {
        context.write(Text.create("bar"));
        context.end(ShellResponse.ok());
      }
    });
    term.publish(TermEvent.close());
    assertJoin(thread);
  }

  public void testProcessClose() throws Exception {
    processor.addListener(term);
    term.publish(TermEvent.readLine("foo"));
    shell.publish(new ShellRunnable() {
      public void run(ShellProcessContext context) throws Exception {
        context.end(ShellResponse.close());
      }
    });
    assertJoin(thread);
  }

  public void testReadLineCancelled() throws Exception {
    final CyclicBarrier syncA = new CyclicBarrier(2);
    final CyclicBarrier syncB = new CyclicBarrier(2);
    final CyclicBarrier syncC = new CyclicBarrier(2);
    final AtomicReference<String> line = new AtomicReference<String>();
    processor.addListener(term);
    term.publish(TermEvent.readLine("foo"));
    shell.publish(new ShellRunnable() {
      public void run(ShellProcessContext context) throws Exception {
        try {
          syncA.await();
          syncB.await();
        }
        catch (Exception e) {
          e.printStackTrace();
        }
        String s = context.readLine("hello", true);
        if (s == null) {
          s = "cancelled";
        }
        line.set(s);
        try {
          syncC.await();
        }
        catch (Exception e) {
          e.printStackTrace();
        }
        context.write(Text.create("foo"));
        context.end(ShellResponse.ok());
      }
    });
    syncA.await();
    term.publish(TermEvent.brk());
    syncB.await();
    syncC.await();
    assertEquals("cancelled", line.get());
    term.publish(TermEvent.close());
    assertJoin(thread);
  }

  public void testReadLineBuffered() throws Exception {
    final CyclicBarrier syncA = new CyclicBarrier(2);
    final CyclicBarrier syncB = new CyclicBarrier(2);
    final CyclicBarrier syncC = new CyclicBarrier(2);
    final AtomicReference<String> line = new AtomicReference<String>();
    processor.addListener(term);
    term.publish(TermEvent.readLine("foo"));
    shell.publish(new ShellRunnable() {
      public void run(ShellProcessContext context) throws Exception {
        try {
          syncA.await();
          syncB.await();
        }
        catch (Exception e) {
          e.printStackTrace();
        }
        String s = context.readLine("hello", true);
        line.set(s);
        try {
          syncC.await();
        }
        catch (Exception e) {
          e.printStackTrace();
        }
        context.end(ShellResponse.close());
      }
    });
    syncA.await();
    term.publish(TermEvent.readLine("bar"));
    syncB.await();
    syncC.await();
    assertEquals("bar", line.get());
    assertJoin(thread);
  }

  public void testReadLineIOException() throws Exception {
    final CyclicBarrier syncA = new CyclicBarrier(2);
    final CyclicBarrier syncB = new CyclicBarrier(2);
    final AtomicReference<String> line = new AtomicReference<String>();
    processor.addListener(term);
    term.publish(TermEvent.readLine("foo"));
    shell.publish(new ShellRunnable() {
      public void run(ShellProcessContext context) throws Exception {
        try {
          syncA.await();
        }
        catch (Exception e) {
          e.printStackTrace();
        }
        String s = context.readLine("hello", true);
        if (s == null) {
          s = "cancelled";
        }
        line.set(s);
        try {
          syncB.await();
        }
        catch (Exception e) {
          e.printStackTrace();
        }
        context.write(Text.create("foo"));
        context.end(ShellResponse.ok());
      }
    });
    syncA.await();
    while (!processor.isWaitingEvent()) {
      // Wait until it's true
    }
    term.publish(new Callable<TermEvent>() {
      public TermEvent call() throws Exception {
        throw new IOException();
      }
    });
    syncB.await();
    assertEquals("cancelled", line.get());
    term.publish(TermEvent.close());
    assertJoin(thread);
  }

  public void testReadLineCancelling() throws Exception {
    final CyclicBarrier syncA = new CyclicBarrier(2);
    final CyclicBarrier syncB = new CyclicBarrier(2);
    final AtomicReference<String> line = new AtomicReference<String>();
    processor.addListener(term);
    term.publish(TermEvent.readLine("foo"));
    shell.publish(new ShellRunnable() {
      public void run(ShellProcessContext context) throws Exception {
        try {
          syncA.await();
        }
        catch (Exception e) {
          e.printStackTrace();
        }
        String s = context.readLine("hello", true);
        if (s == null) {
          s = "cancelled";
        }
        line.set(s);
        try {
          syncB.await();
        }
        catch (Exception e) {
          e.printStackTrace();
        }
        context.write(Text.create("foo"));
        context.end(ShellResponse.ok());
      }
    });
    syncA.await();
    while (!processor.isWaitingEvent()) {
      // Wait until it's true
    }
    term.publish(TermEvent.brk());
    syncB.await();
    assertEquals("cancelled", line.get());
    term.publish(TermEvent.close());
    assertJoin(thread);
  }
}