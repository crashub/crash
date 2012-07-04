package org.crsh.shell.impl.remoting;

import org.crsh.cmdline.CommandCompletion;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellProcess;
import org.crsh.util.CloseableList;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ClientAutomaton implements Runnable {

  /** . */
  final Shell shell;

  /** . */
  final ObjectOutputStream out;

  /** . */
  final ObjectInputStream in;

  /** . */
  ClientProcessContext current;

  /** . */
  final CloseableList listeners;

  /** . */
  Integer width;

  public ClientAutomaton(ObjectOutputStream out, ObjectInputStream in, Shell shell) {
    CloseableList listeners = new CloseableList();
    listeners.add(in);
    listeners.add(out);

    //
    this.in = in;
    this.out = out;
    this.shell = shell;
    this.listeners = listeners;
  }

  public ClientAutomaton(InputStream in,OutputStream out, Shell shell) throws IOException {
    this(new ObjectOutputStream(out), new ObjectInputStream(in), shell);
  }

  public ClientAutomaton addCloseListener(Closeable closeable) {
    listeners.add(closeable);
    return this;
  }

  public void run() {
    try {
      while (!listeners.isClosed()) {
        ClientMessage msg = (ClientMessage)in.readObject();
        switch (msg) {
          case GET_WELCOME:
            String welcome = shell.getWelcome();
            out.writeObject(welcome);
            out.flush();
            break;
          case GET_PROMPT:
            String prompt = shell.getPrompt();
            out.writeObject(prompt);
            out.flush();
            break;
          case GET_COMPLETION:
            String prefix = (String)in.readObject();
            CommandCompletion completion = shell.complete(prefix);
            out.writeObject(completion);
            out.flush();
            break;
          case EXECUTE:
            width = (Integer) in.readObject();
            String line = (String)in.readObject();
            ShellProcess process = shell.createProcess(line);
            current = new ClientProcessContext(this, process);
            process.execute(current);
            break;
          case CANCEL:
            if (current != null) {
              current.process.cancel();
            }
            break;
          case CLOSE:
            close();
            break;
        }
      }
    }
    catch (Exception e) {
//      e.printStackTrace();
      //
    }
  }

  void close() {
    listeners.close();
  }

  public int getWidth() {
    return width;
  }
  
}
