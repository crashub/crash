package org.crsh.shell.impl.remoting;

import org.crsh.cmdline.CommandCompletion;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellProcessContext;
import org.crsh.shell.ShellResponse;
import org.crsh.util.CloseableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.UndeclaredThrowableException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ServerAutomaton implements Shell {

  /** . */
  final Logger log = LoggerFactory.getLogger(ServerAutomaton.class);

  /** . */
  final ObjectInputStream in;

  /** . */
  final ObjectOutputStream out;

  /** . */
  ServerProcess process;

  /** . */
  final CloseableList listeners;

  public ServerAutomaton(ObjectOutputStream out, ObjectInputStream in) {
    CloseableList listeners = new CloseableList();
    listeners.add(in);
    listeners.add(out);

    //
    this.in = in;
    this.out = out;
    this.listeners = listeners;
  }

  public ServerAutomaton(InputStream in, OutputStream out) throws IOException {
    this(new ObjectOutputStream(out), new ObjectInputStream(in));
  }

  public ServerAutomaton addCloseListener(Closeable closeable) {
    listeners.add(closeable);
    return this;
  }

  public String getWelcome() {
    try {
      out.writeObject(ClientMessage.GET_WELCOME);
      out.flush();
      return (String)in.readObject();
    }
    catch (Exception e) {
      throw new UndeclaredThrowableException(e);
    }
  }

  public String getPrompt() {
    try {
      out.writeObject(ClientMessage.GET_PROMPT);
      out.flush();
      return (String)in.readObject();
    }
    catch (Exception e) {
      throw new UndeclaredThrowableException(e);
    }
  }

  public ShellProcess createProcess(String request) throws IllegalStateException {
    return new ServerProcess(this, request);
  }

  public CommandCompletion complete(String prefix) {
    try {
      out.writeObject(ClientMessage.GET_COMPLETION);
      out.writeObject(prefix);
      out.flush();
      return (CommandCompletion)in.readObject();
    }
    catch (Exception e) {
      throw new UndeclaredThrowableException(e);
    }
  }

  void close() {
    listeners.close();
  }

  void execute(ServerProcess process, ShellProcessContext processContext) throws IllegalStateException {

    if (this.process == null) {
      this.process = process;
    } else {
      throw new IllegalStateException();
    }

    //
    ShellResponse response = null;
    try {
      out.writeObject(ClientMessage.EXECUTE);
      out.writeObject(process.line);
      out.flush();

      //
      while (response == null) {
        ServerMessage msg = (ServerMessage)in.readObject();
        switch (msg) {
          case GET_WIDTH:
            int width = processContext.getWidth();
            out.writeObject(width);
            out.flush();
            break;
          case READLINE:
            String request = (String)in.readObject();
            boolean echo = (Boolean)in.readObject();
            String line = processContext.readLine(request, echo);
            out.writeObject(line);
            out.flush();
            break;
          case END:
            response = (ShellResponse)in.readObject();
            break;
          default:
            response = ShellResponse.internalError("Unexpected");
            break;
        }
      }
    }
    catch (Exception e) {
      log.error("Remoting issue", e);
      response = ShellResponse.internalError("Remoting issue", e);
    }
    finally {

      //
      this.process = null;

      //
      if (response != null) {
        processContext.end(response);
      } else {
        processContext.end(ShellResponse.internalError(""));
      }
    }
  }

  void cancel(ServerProcess process) throws IllegalStateException {
    if (process == this.process) {
      this.process = null;
      try {
        out.writeObject(ClientMessage.CANCEL);
        out.flush();
      }
      catch (IOException ignore) {
      }
    }
  }
}
