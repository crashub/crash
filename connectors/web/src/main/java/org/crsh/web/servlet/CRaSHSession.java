package org.crsh.web.servlet;

import org.crsh.shell.Shell;

import javax.websocket.Session;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

/** @author Julien Viet */
class CRaSHSession {

  /** . */
  final Session wsSession;

  /** . */
  final Shell shell;

  /** The current process being executed. */
  final AtomicReference<WSProcessContext> current;

  CRaSHSession(Session wsSession, Shell shell) {
    this.wsSession = wsSession;
    this.shell = shell;
    this.current = new AtomicReference<WSProcessContext>();
  }

  void send(String type) {
    send(type, null);
  }

  void send(String type, Object data) {
    send(new Event(type, data));
  }

  private void send(Event event) {
    try {
      wsSession.getBasicRemote().sendText(event.toJSON());
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }
}
