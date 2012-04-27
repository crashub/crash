package org.crsh.shell.remoting;

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
import org.crsh.shell.ShellResponse;
import org.crsh.shell.concurrent.AsyncShell;
import org.crsh.util.PipedChannel;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
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
          protected ShellResponse execute(String request) {
            return ShellResponse.display("juu");
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
    assertEquals("juu", response.getText());

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
          protected ShellResponse execute(String request) {
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
            return ShellResponse.display("juu");
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
