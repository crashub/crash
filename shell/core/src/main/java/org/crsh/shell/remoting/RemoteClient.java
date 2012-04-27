package org.crsh.shell.remoting;

import org.crsh.shell.Shell;
import org.crsh.util.AbstractSocketClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RemoteClient extends AbstractSocketClient {

  /** . */
  private ClientAutomaton automaton;

  /** . */
  private final Shell shell;

  public RemoteClient(int port, Shell shell) {
    super(port);

    //
    this.shell = shell;
  }

  @Override
  protected void handle(InputStream in, OutputStream out) throws IOException {
    this.automaton = new ClientAutomaton(in, out, shell).addCloseListener(this);;
  }

  public Runnable getRunnable() {
    return automaton;
  }
}
