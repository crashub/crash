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

package org.crsh.shell.impl.remoting;

import org.crsh.AbstractTestCase;
import test.shell.base.BaseProcess;
import test.shell.base.BaseProcessContext;
import test.shell.base.BaseProcessFactory;
import test.shell.base.BaseShell;
import org.crsh.cli.impl.completion.CompletionMatch;
import org.crsh.cli.impl.Delimiter;
import org.crsh.cli.spi.Completion;
import org.crsh.shell.ErrorKind;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellProcessContext;
import org.crsh.shell.ShellResponse;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class RemoteShellTestCase extends AbstractTestCase {

  static class ClientProcessor extends Thread {

    /** . */
    final ObjectInputStream in;

    /** . */
    final ObjectOutputStream out;

    /** . */
    final Shell shell;

    ClientProcessor(ObjectInputStream in, ObjectOutputStream out, Shell shell) {
      this.in = in;
      this.out = out;
      this.shell = shell;
    }

    @Override
    public void run() {
      ClientAutomaton client = new ClientAutomaton(out, in, shell);
      client.run();
    }
  }

  /** . */
  private ObjectInputStream serverOIS;

  /** . */
  private ObjectOutputStream clientOOS;

  /** . */
  private ObjectInputStream clientOIS;

  /** . */
  private ObjectOutputStream serverOOS;

  @Override
  protected void setUp() throws Exception {

    PipedInputStream a = new PipedInputStream();
    PipedOutputStream b = new PipedOutputStream(a);

    PipedInputStream c = new PipedInputStream();
    PipedOutputStream d = new PipedOutputStream(c);

    //
    ObjectOutputStream clientOOS = new ObjectOutputStream(b);
    clientOOS.flush();
    ObjectOutputStream serverOOS = new ObjectOutputStream(d);
    serverOOS.flush();
    ObjectInputStream serverOIS = new ObjectInputStream(a);
    ObjectInputStream clientOIS = new ObjectInputStream(c);

    //
    this.clientOIS = clientOIS;
    this.clientOOS = clientOOS;
    this.serverOIS = serverOIS;
    this.serverOOS = serverOOS;
  }

  public void testSerialization() throws Exception {

    ServerMessage message = new ServerMessage.Completion(new CompletionMatch(Delimiter.DOUBLE_QUOTE, Completion.create("pref", "ix", true)));
    clientOOS.writeObject(message);
    clientOOS.flush();
    ServerMessage after = (ServerMessage)serverOIS.readObject();
    System.out.println("after = " + after);

  }

  public void testPrompt() throws Exception {
    ClientProcessor t = new ClientProcessor(clientOIS, clientOOS, new BaseShell() {
      @Override
      public String getPrompt() {
        return "foo";
      }
    });
    t.start();

    //
    ServerAutomaton server = new ServerAutomaton(serverOOS, serverOIS);
    assertEquals("foo", server.getPrompt());

    //
    t.interrupt();
    assertJoin(t);
  }

  public void testWelcome() throws Exception {
    ClientProcessor t = new ClientProcessor(clientOIS, clientOOS, new BaseShell() {
      @Override
      public String getWelcome() {
        return "bar";
      }
    });
    t.start();

    //
    ServerAutomaton server = new ServerAutomaton(serverOOS, serverOIS);
    assertEquals("bar", server.getWelcome());

    //
    t.interrupt();
    assertJoin(t);
  }

  public void testExecute() throws Exception {
    ClientProcessor t = new ClientProcessor(clientOIS, clientOOS, new BaseShell(new BaseProcessFactory() {
      @Override
      public BaseProcess create(String request) {
        return new BaseProcess(request) {
          @Override
          public void process(String request, ShellProcessContext processContext) throws IOException {
            processContext.append("juu");
            processContext.end(ShellResponse.ok());
          }
        };
      }
    }));
    t.start();

    //
    ServerAutomaton server = new ServerAutomaton(serverOOS, serverOIS);

    ShellProcess process = server.createProcess("hello");
    BaseProcessContext context = BaseProcessContext.create(process);
    context.execute();
    assertInstance(ShellResponse.Ok.class, context.getResponse());
    assertEquals("juu", context.getOutput());

    //
    t.interrupt();
    assertJoin(t);
  }

  public void testClose() throws Exception {
    ClientProcessor t = new ClientProcessor(clientOIS, clientOOS, new BaseShell(new BaseProcessFactory() {
      @Override
      public BaseProcess create(String request) {
        return new BaseProcess(request) {
          @Override
          protected ShellResponse execute(String request) {
            return ShellResponse.close();
          }
        };
      }
    }));
    t.start();

    //
    ServerAutomaton server = new ServerAutomaton(serverOOS, serverOIS);

    ShellProcess process = server.createProcess("hello");
    BaseProcessContext context = BaseProcessContext.create(process);
    context.execute();
    ShellResponse response = context.getResponse();
    assertInstance(ShellResponse.Close.class, response);

    //
    assertJoin(t);
  }

  public void testRawClose() throws Exception {
    ClientProcessor t = new ClientProcessor(clientOIS, clientOOS, new BaseShell(new BaseProcessFactory() {
      @Override
      public BaseProcess create(String request) {
        return new BaseProcess(request) {
          @Override
          protected ShellResponse execute(String request) {
            return ShellResponse.close();
          }
        };
      }
    }));
    t.start();

    //
    serverOOS.writeObject(new ClientMessage.Execute(32, 50, ""));
    serverOOS.flush();
    ServerMessage.End message = (ServerMessage.End)serverOIS.readObject();
    assertInstance(ShellResponse.Close.class, message.response);

    // This should fail at some point
    try {
      serverOIS.readObject();
      fail();
    }
    catch (IOException e) {
      // OK
    }

    //
    assertJoin(t);
  }

  public void testExceptionDuringRequest() throws Exception {

    final CountDownLatch latch = new CountDownLatch(1);
    final AtomicReference<RuntimeException> ex = new AtomicReference<RuntimeException>();

    ClientProcessor t = new ClientProcessor(clientOIS, clientOOS, new BaseShell(new BaseProcessFactory() {
      int count = 0;
      @Override
      public BaseProcess create(String request) {
        return new BaseProcess(request) {
          @Override
          public void process(String request, final ShellProcessContext processContext) throws IOException {
            if (count == 0) {
              count = 1;
              new Thread() {
                @Override
                public void run() {
                  try {
                    latch.await();
                  }
                  catch (InterruptedException e) {
                  }
                  processContext.end(ShellResponse.ok());
                }
              }.start();
              ex.set(new RuntimeException("this is a runtime exception"));
              throw ex.get();
            } else {
              processContext.end(ShellResponse.ok());
            }
          }
        };
      }
    }));
    t.start();

    //
    serverOOS.writeObject(new ClientMessage.Execute(32, 50, ""));
    serverOOS.flush();

    //
    ServerMessage.End message = (ServerMessage.End)serverOIS.readObject();
    ShellResponse.Error error = assertInstance(ShellResponse.Error.class, message.response);
    assertEquals(ErrorKind.INTERNAL, error.getKind());
    assertInstance(Exception.class, error.getThrowable());
    assertEquals("this is a runtime exception", error.getThrowable().getMessage());
    assertEquals(Arrays.asList(ex.get().getStackTrace()), Arrays.asList(error.getThrowable().getStackTrace()));

    //
    latch.countDown();

    //
    serverOOS.writeObject(new ClientMessage.Execute(32, 50, ""));
    serverOOS.flush();

    //
    message = (ServerMessage.End)serverOIS.readObject();
    assertInstance(ShellResponse.Ok.class, message.response);

    //
    t.interrupt();
    assertJoin(t);
  }

  public void testCancel() throws Exception {

    final AtomicBoolean waiting = new AtomicBoolean();
    final CountDownLatch latch = new CountDownLatch(1);

    //
    ClientProcessor t = new ClientProcessor(clientOIS, clientOOS, new BaseShell(new BaseProcessFactory() {
      @Override
      public BaseProcess create(String request) {
        return new BaseProcess(request) {
          @Override
          public void process(String request, final ShellProcessContext processContext) throws IOException {
            new Thread() {
              @Override
              public void run() {
                synchronized (waiting) {
                  if (waiting.get()) {
                    waiting.notifyAll();
                  } else {
                    waiting.set(true);
                  }
                  try {
                    waiting.wait();
                  }
                  catch (InterruptedException e) {
                    e.printStackTrace();
                  }
                }
                try {
                  processContext.append("juu");
                  processContext.end(ShellResponse.ok());
                }
                catch (IOException e) {
                  e.printStackTrace();
                }
                latch.countDown();
              }
            }.start();
          }
          @Override
          public void cancel() {
            synchronized (waiting) {
              waiting.notifyAll();
            }
          }
        };
      }
    }));
    t.start();

    //
    ServerAutomaton server = new ServerAutomaton(serverOOS, serverOIS);
    ShellProcess process = server.createProcess("hello");
    final BaseProcessContext context = BaseProcessContext.create(process);

    //
    final AtomicReference<Throwable> error = new AtomicReference<Throwable>();
    Thread u = new Thread() {
      @Override
      public void run() {
        context.execute();
        ShellResponse response = context.getResponse();
        assertInstance(ShellResponse.Cancelled.class, response);
      }
    };
    u.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
      public void uncaughtException(Thread t, Throwable e) {
        error.set(e);
      }
    });
    u.start();

    //
    synchronized (waiting) {
      if (!waiting.get()) {
        waiting.set(true);
        waiting.wait();
      }
    }

    //
    process.cancel();

    //
    latch.await();

    //
    t.interrupt();
    assertJoin(t);
    assertJoin(u);
    if (error.get() != null) {
      throw failure(error.get());
    }
  }

  public void testComplete() {
    ClientProcessor t = new ClientProcessor(clientOIS, clientOOS, new BaseShell() {
      @Override
      public CompletionMatch complete(String prefix) {
        return new CompletionMatch(Delimiter.DOUBLE_QUOTE, Completion.create(prefix, "ix", true));
      }
    });
    t.start();

    //
    ServerAutomaton server = new ServerAutomaton(serverOOS, serverOIS);
    CompletionMatch completion = server.complete("pref");
    assertEquals(Delimiter.DOUBLE_QUOTE, completion.getDelimiter());
    Completion value = completion.getValue();
    assertEquals("pref", value.getPrefix());
    assertEquals(1, value.getSize());
    assertEquals(Collections.singleton("ix"), value.getValues());
    assertEquals(Boolean.TRUE, value.get("ix"));

    //
    t.interrupt();
    assertJoin(t);
  }
}
