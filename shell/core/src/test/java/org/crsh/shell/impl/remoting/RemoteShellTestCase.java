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
import org.crsh.BaseProcess;
import org.crsh.BaseProcessContext;
import org.crsh.BaseProcessFactory;
import org.crsh.BaseShell;
import org.crsh.cmdline.CommandCompletion;
import org.crsh.cmdline.Delimiter;
import org.crsh.cmdline.spi.ValueCompletion;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellProcessContext;
import org.crsh.shell.ShellResponse;
import org.crsh.shell.impl.async.AsyncShell;
import org.crsh.text.Text;
import org.crsh.util.PipedChannel;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

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

    PipedChannel a = new PipedChannel();
    PipedChannel b = new PipedChannel();

    //
    ObjectOutputStream clientOOS = new ObjectOutputStream(a.getOut());
    clientOOS.flush();
    ObjectOutputStream serverOOS = new ObjectOutputStream(b.getOut());
    serverOOS.flush();
    ObjectInputStream serverOIS = new ObjectInputStream(a.getIn());
    ObjectInputStream clientOIS = new ObjectInputStream(b.getIn());

    //
    this.clientOIS = clientOIS;
    this.clientOOS = clientOOS;
    this.serverOIS = serverOIS;
    this.serverOOS = serverOOS;
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
            processContext.provide(new Text("juu"));
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
    serverOOS.writeObject(ClientMessage.EXECUTE);
    serverOOS.writeObject(32);
    serverOOS.writeObject(40);
    serverOOS.writeObject("");
    serverOOS.flush();
    ServerMessage proto = (ServerMessage)serverOIS.readObject();
    assertEquals(ServerMessage.END, proto);
    ShellResponse response = (ShellResponse)serverOIS.readObject();
    assertInstance(ShellResponse.Close.class, response);

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


  public void testCancel() throws Exception {

    final AtomicBoolean waiting = new AtomicBoolean();
    final Object lock = new Object();

    ClientProcessor t = new ClientProcessor(clientOIS, clientOOS, new AsyncShell(Executors.newCachedThreadPool(), new BaseShell(new BaseProcessFactory() {
      @Override
      public BaseProcess create(String request) {
        return new BaseProcess(request) {
          @Override
          public void process(String request, ShellProcessContext processContext) throws IOException {
            synchronized (lock) {
              if (waiting.get()) {
                lock.notifyAll();
              } else {
                waiting.set(true);
              }
              try {
                lock.wait();
              }
              catch (InterruptedException e) {
                e.printStackTrace();
              }
            }
            processContext.provide(new Text("juu"));
            processContext.end(ShellResponse.ok());
          }
          @Override
          public void cancel() {
            synchronized (lock) {
              lock.notifyAll();
            }
          }
        };
      }
    })));
    t.start();

    //
    ServerAutomaton server = new ServerAutomaton(serverOOS, serverOIS);
    ShellProcess process = server.createProcess("hello");
    final BaseProcessContext context = BaseProcessContext.create(process);

    Thread u = new Thread() {
      @Override
      public void run() {
        context.execute();
        ShellResponse response = context.getResponse();
        assertInstance(ShellResponse.Cancelled.class, response);
      }
    };
    u.start();

    //
    synchronized (lock) {
      if (!waiting.get()) {
        waiting.set(true);
        lock.wait();
      }
    }

    //
    process.cancel();


    //
    t.interrupt();
    assertJoin(t);
  }

  public void testComplete() {
    ClientProcessor t = new ClientProcessor(clientOIS, clientOOS, new BaseShell() {
      @Override
      public CommandCompletion complete(String prefix) {
        return new CommandCompletion(Delimiter.DOUBLE_QUOTE, new ValueCompletion(prefix, Collections.singletonMap("ix", true)));
      }
    });
    t.start();

    //
    ServerAutomaton server = new ServerAutomaton(serverOOS, serverOIS);
    CommandCompletion completion = server.complete("pref");
    assertEquals(Delimiter.DOUBLE_QUOTE, completion.getDelimiter());
    ValueCompletion value = completion.getValue();
    assertEquals("pref", value.getPrefix());
    assertEquals(1, value.getSize());
    assertEquals(Collections.singleton("ix"), value.getSuffixes());
    assertEquals(true, value.get("ix"));

    //
    t.interrupt();
    assertJoin(t);
  }
}
