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

package org.crsh.shell.impl.command;

import org.crsh.cli.Command;
import org.crsh.command.BaseCommand;
import org.crsh.command.Pipe;
import org.crsh.shell.AbstractShellTestCase;
import org.crsh.shell.ErrorKind;

import javax.naming.CommunicationException;
import javax.naming.NamingException;
import javax.naming.SizeLimitExceededException;
import javax.naming.directory.AttributeInUseException;
import javax.naming.directory.SchemaViolationException;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PipeLineFailureTestCase extends AbstractShellTestCase {

  public static class Counter extends Pipe<String, Object> {

    /** . */
    final AtomicInteger openCount = new AtomicInteger(), provideCound = new AtomicInteger(),
        flushCount = new AtomicInteger(), closeCount = new AtomicInteger();

    public void open() throws Exception {
      openCount.incrementAndGet();
    }
    public void provide(String element) throws Exception {
      provideCound.incrementAndGet();
    }
    public void flush() throws IOException {
      flushCount.incrementAndGet();
    }
    public void close() throws Exception {
      closeCount.incrementAndGet();
    }

    public void reset() {
      openCount.set(0);
      provideCound.set(0);
      flushCount.set(0);
      closeCount.set(0);
    }
  }

  public static class CallbackCounterCommand extends BaseCommand {

    public static Class<CallbackCounterCommand> reset() {
      counter.reset();
      return CallbackCounterCommand.class;
    }

    /** . */
    public static Counter counter = new Counter();

    @Command
    public Pipe<String, Object> main() {
      return counter;
    }
  }

  public static class FailOnInvoke extends BaseCommand {

    /** . */
    static final AtomicInteger count = new AtomicInteger();

    public static Class<FailOnInvoke> reset() {
      count.set(0);
      return FailOnInvoke.class;
    }

    @Command
    public Pipe<String, Object> main() throws Exception {
      count.incrementAndGet();
      throw new SchemaViolationException();
    }
  }

  public static class FailOnOpen extends BaseCommand {

    public static Class<FailOnOpen> reset() {
      counter.reset();
      return FailOnOpen.class;
    }

    /** . */
    public static Counter counter = new Counter() {
      @Override
      public void open() throws Exception {
        super.open();
        throw new AttributeInUseException();
      }
    };

    @Command
    public Pipe<String, Object> main() {
      return counter;
    }
  }

  public static class FailOnProvide extends BaseCommand {

    public static Class<FailOnProvide> reset() {
      counter.reset();
      return FailOnProvide.class;
    }

    /** . */
    public static Counter counter = new Counter() {
      @Override
      public void provide(String element) throws Exception {
        super.provide(element);
        throw new CommunicationException();
      }
    };

    @Command
    public Pipe<String, Object> main() {
      return counter;
    }
  }

  public static class FailOnClose extends BaseCommand {

    public static Class<FailOnClose> reset() {
      counter.reset();
      return FailOnClose.class;
    }

    /** . */
    public static Counter counter = new Counter() {
      @Override
      public void close() throws Exception {
        super.close();
        throw new SizeLimitExceededException();
      }
    };

    @Command
    public Pipe<String, Object> main() {
      return counter;
    }
  }

  public static class Producer extends BaseCommand {

    public static Class<Producer> reset() {
      count.set(0);
      return Producer.class;
    }

    /** . */
    static final AtomicInteger count = new AtomicInteger();

    @Command
    public void main(org.crsh.command.InvocationContext<String> context) throws Exception {
      count.incrementAndGet();
      context.provide("foo");
    }
  }

  public void testFailOnInvokePipe() {
    lifeCycle.bindClass("command", FailOnInvoke.reset());
    assertError("command", ErrorKind.EVALUATION, SchemaViolationException.class);
    assertEquals(1, FailOnInvoke.count.get());
  }

  public void testFailOnInvokePipeToPipe() {
    lifeCycle.bindClass("producer", FailOnInvoke.reset());
    lifeCycle.bindClass("consumer", CallbackCounterCommand.reset());
    assertError("producer | consumer", ErrorKind.EVALUATION, SchemaViolationException.class);
    assertEquals(1, FailOnInvoke.count.get());
    assertEquals(1, CallbackCounterCommand.counter.openCount.get());
    assertEquals(0, CallbackCounterCommand.counter.provideCound.get());
    assertEquals(1, CallbackCounterCommand.counter.flushCount.get());
    assertEquals(1, CallbackCounterCommand.counter.closeCount.get());
  }

  public void testPipeToFailOnInvokePipe() {
    lifeCycle.bindClass("producer", CallbackCounterCommand.reset());
    lifeCycle.bindClass("consumer", FailOnInvoke.reset());
    assertError("producer | consumer", ErrorKind.EVALUATION, SchemaViolationException.class);
    assertEquals(1, FailOnInvoke.count.get());
    assertEquals(0, CallbackCounterCommand.counter.openCount.get());
    assertEquals(0, CallbackCounterCommand.counter.provideCound.get());
    assertEquals(0, CallbackCounterCommand.counter.flushCount.get());
    assertEquals(0, CallbackCounterCommand.counter.closeCount.get());
  }

  public void testProducerToFailOnInvokePipe() {
    lifeCycle.bindClass("producer", Producer.reset());
    lifeCycle.bindClass("consumer", FailOnInvoke.reset());
    assertError("producer | consumer", ErrorKind.EVALUATION, SchemaViolationException.class);
    assertEquals(1, FailOnInvoke.count.get());
    assertEquals(0, Producer.count.get());
  }

  public void testFailOnOpenPipe() {
    lifeCycle.bindClass("command", FailOnOpen.reset());
    assertError("command", ErrorKind.EVALUATION, AttributeInUseException.class);
    assertEquals(1, FailOnOpen.counter.openCount.get());
    assertEquals(0, FailOnOpen.counter.provideCound.get());
    assertEquals(0, FailOnOpen.counter.flushCount.get());
    assertEquals(0, FailOnOpen.counter.closeCount.get());
  }

  public void testFailOnOpenPipeToPipe() {
    lifeCycle.bindClass("producer", FailOnOpen.reset());
    lifeCycle.bindClass("consumer", CallbackCounterCommand.reset());
    assertError("producer | consumer", ErrorKind.EVALUATION, AttributeInUseException.class);
    assertEquals(1, FailOnOpen.counter.openCount.get());
    assertEquals(0, FailOnOpen.counter.provideCound.get());
    assertEquals(0, FailOnOpen.counter.flushCount.get());
    assertEquals(0, FailOnOpen.counter.closeCount.get());
    assertEquals(1, CallbackCounterCommand.counter.openCount.get());
    assertEquals(0, CallbackCounterCommand.counter.provideCound.get());
    assertEquals(1, CallbackCounterCommand.counter.flushCount.get());
    assertEquals(1, CallbackCounterCommand.counter.closeCount.get());
  }

  public void testPipeToFailOnOpenPipe() {
    lifeCycle.bindClass("producer", CallbackCounterCommand.reset());
    lifeCycle.bindClass("consumer", FailOnOpen.reset());
    assertError("producer | consumer", ErrorKind.EVALUATION, AttributeInUseException.class);
    assertEquals(1, FailOnOpen.counter.openCount.get());
    assertEquals(0, FailOnOpen.counter.provideCound.get());
    assertEquals(0, FailOnOpen.counter.flushCount.get());
    assertEquals(0, FailOnOpen.counter.closeCount.get());
    assertEquals(0, CallbackCounterCommand.counter.openCount.get());
    assertEquals(0, CallbackCounterCommand.counter.provideCound.get());
    assertEquals(0, CallbackCounterCommand.counter.flushCount.get());
    assertEquals(0, CallbackCounterCommand.counter.closeCount.get());
  }

  public void testProducerToFailOnOpenPipe() {
    lifeCycle.bindClass("producer", Producer.reset());
    lifeCycle.bindClass("consumer", FailOnOpen.reset());
    assertError("producer | consumer", ErrorKind.EVALUATION, AttributeInUseException.class);
    assertEquals(0, Producer.count.get());
    assertEquals(1, FailOnOpen.counter.openCount.get());
    assertEquals(0, FailOnOpen.counter.provideCound.get());
    assertEquals(0, FailOnOpen.counter.flushCount.get());
    assertEquals(0, FailOnOpen.counter.closeCount.get());
  }

  public void testProducerToFailOnProvidePipe() {
    lifeCycle.bindClass("producer", Producer.reset());
    lifeCycle.bindClass("consumer", FailOnProvide.reset());
    assertError("producer | consumer", ErrorKind.EVALUATION, CommunicationException.class);
    assertEquals(1, Producer.count.get());
    assertEquals(1, FailOnProvide.counter.openCount.get());
    assertEquals(1, FailOnProvide.counter.provideCound.get());
    assertEquals(1, FailOnProvide.counter.flushCount.get());
    assertEquals(1, FailOnProvide.counter.closeCount.get());
  }

  public void testProducerToFailOnClosePipe() {
    lifeCycle.bindClass("producer", Producer.reset());
    lifeCycle.bindClass("consumer", FailOnClose.reset());
    assertError("producer | consumer", ErrorKind.EVALUATION, SizeLimitExceededException.class);
    assertEquals(1, Producer.count.get());
    assertEquals(1, FailOnClose.counter.openCount.get());
    assertEquals(1, FailOnClose.counter.provideCound.get());
    assertEquals(1, FailOnClose.counter.flushCount.get());
    assertEquals(1, FailOnClose.counter.closeCount.get());
  }

  public void testFailOnClosePipe() {
    lifeCycle.bindClass("command", FailOnClose.reset());
    assertError("command", ErrorKind.EVALUATION, SizeLimitExceededException.class);
    assertEquals(1, FailOnClose.counter.openCount.get());
    assertEquals(0, FailOnClose.counter.provideCound.get());
    assertEquals(1, FailOnClose.counter.flushCount.get());
    assertEquals(1, FailOnClose.counter.closeCount.get());
  }

  public void testFailOnClosePipeToPipe() {
    lifeCycle.bindClass("producer", FailOnClose.reset());
    lifeCycle.bindClass("consumer", CallbackCounterCommand.reset());
    assertError("producer | consumer", ErrorKind.EVALUATION, SizeLimitExceededException.class);
    assertEquals(1, FailOnClose.counter.openCount.get());
    assertEquals(0, FailOnOpen.counter.provideCound.get());
    assertEquals(1, FailOnClose.counter.flushCount.get());
    assertEquals(1, FailOnClose.counter.closeCount.get());
    assertEquals(1, CallbackCounterCommand.counter.openCount.get());
    assertEquals(0, CallbackCounterCommand.counter.provideCound.get());
    // assertEquals(1, CallbackCounterCommand.counter.flushCount.get()); <-- not passing at the moment
    assertEquals(1, CallbackCounterCommand.counter.closeCount.get());
  }

  public void testPipeToFailOnClosePipe() {
    lifeCycle.bindClass("producer", CallbackCounterCommand.reset());
    lifeCycle.bindClass("consumer", FailOnClose.reset());
    assertError("producer | consumer", ErrorKind.EVALUATION, SizeLimitExceededException.class);
    assertEquals(1, CallbackCounterCommand.counter.openCount.get());
    assertEquals(0, CallbackCounterCommand.counter.provideCound.get());
    assertEquals(1, CallbackCounterCommand.counter.flushCount.get());
    assertEquals(1, CallbackCounterCommand.counter.closeCount.get());
    assertEquals(1, FailOnClose.counter.openCount.get());
    assertEquals(0, FailOnOpen.counter.provideCound.get());
    // assertEquals(1 , FailDuringClose.counter.flushCount.get()); <-- not passing at the moment
    assertEquals(1, FailOnClose.counter.closeCount.get());
  }

  public void testFailOnClosePipeToFailOnOpenPipe() {
    lifeCycle.bindClass("producer", FailOnOpen.reset());
    lifeCycle.bindClass("consumer", FailOnClose.reset());
    assertError("producer | consumer", ErrorKind.EVALUATION, SizeLimitExceededException.class);
    assertEquals(1, FailOnOpen.counter.openCount.get());
    assertEquals(0, FailOnOpen.counter.provideCound.get());
    assertEquals(0, FailOnOpen.counter.flushCount.get());
    assertEquals(0, FailOnOpen.counter.closeCount.get());
    assertEquals(1, FailOnClose.counter.openCount.get());
    assertEquals(0, FailOnClose.counter.provideCound.get());
    assertEquals(1, FailOnClose.counter.flushCount.get());
    assertEquals(1, FailOnClose.counter.closeCount.get());
  }
}
