package org.crsh.shell.remoting;

import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellProcessContext;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ServerProcess implements ShellProcess {

  /** . */
  final ServerAutomaton server;

  /** . */
  final String line;

  /** . */
  private int status;

  ServerProcess(ServerAutomaton server, String line) {
    this.server = server;
    this.line = line;
    this.status = 0;
  }

  public void execute(ShellProcessContext processContext) throws IllegalStateException {
    if (status != 0) {
      throw new IllegalStateException();
    }
    status = 1;
    try {
      server.execute(this, processContext);
    }
    finally {
      status = 2;
    }
  }

  public void cancel() {
    switch (status) {
      case 0:
        throw new IllegalStateException();
      case 1:
        server.cancel(this);
        break;
    }
  }
}
