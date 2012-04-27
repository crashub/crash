package org.crsh.shell.remoting;

import org.crsh.shell.Shell;
import org.crsh.util.AbstractSocketServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RemoteServer extends AbstractSocketServer {

  /** . */
  private ServerAutomaton automaton;

  public RemoteServer(int bindingPort) {
    super(bindingPort);
  }

  @Override
  protected void handle(InputStream in, OutputStream out) throws IOException {
    this.automaton = new ServerAutomaton(in, out).addCloseListener(this);
  }

  public Shell getShell() {
    return automaton;
  }
}
