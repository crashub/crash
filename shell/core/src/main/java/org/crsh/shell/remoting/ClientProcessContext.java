package org.crsh.shell.remoting;

import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellProcessContext;
import org.crsh.shell.ShellResponse;

import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class ClientProcessContext implements ShellProcessContext {

  /** . */
  final ClientAutomaton client;

  /** . */
  final ShellProcess process;

  ClientProcessContext(ClientAutomaton client, ShellProcess process) {
    this.client = client;
    this.process = process;
  }

  public int getWidth() {
    try {
      client.out.writeObject(ServerMessage.GET_WIDTH);
      client.out.flush();
      return (Integer)client.in.readObject();
    }
    catch (Exception e) {
      return 80;
    }
  }

  public String getProperty(String name) {
    return null;
  }

  public String readLine(String msg, boolean echo) {
    try {
      client.out.writeObject(ServerMessage.READLINE);
      client.out.writeObject(msg);
      client.out.writeObject(echo);
      client.out.flush();
      return (String)client.in.readObject();
    }
    catch (Exception e) {
      return null;
    }
  }

  public void end(ShellResponse response) {
    try {
      client.current = null;
      client.out.writeObject(ServerMessage.END);
      client.out.writeObject(response);
      client.out.flush();
    }
    catch (IOException ignore) {
      //
    }
    finally {
      if (response instanceof ShellResponse.Close) {
        client.close();
      }
    }
  }
}
