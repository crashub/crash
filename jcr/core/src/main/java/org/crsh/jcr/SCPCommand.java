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
package org.crsh.jcr;

import org.apache.sshd.server.Environment;
import org.crsh.ssh.term.AbstractCommand;
import org.crsh.ssh.term.SSHLifeCycle;

import javax.jcr.Credentials;
import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class SCPCommand extends AbstractCommand implements Runnable {

  /** . */
  protected final Logger log = Logger.getLogger(getClass().getName());

  /** . */
  protected static final int OK = 0;

  /** . */
  protected static final int ERROR = 2;

  /** . */
  private Thread thread;

  /** . */
  private Environment env;

  /** . */
  private final String target;

  protected SCPCommand(String target) {
    this.target = target;
  }

  /**
   * Read from the input stream an exact amount of bytes.
   *
   * @param length the expected data length to read
   * @return an input stream for reading
   * @throws IOException any io exception
   */
  final InputStream read(final int length) throws IOException {
    log.log(Level.FINE, "Returning stream for length " + length);
    return new InputStream() {

      /** How many we've read so far. */
      int count = 0;

      @Override
      public int read() throws IOException {
        if (count < length) {
          int value = in.read();
          if (value == -1) {
            throw new IOException("Abnormal end of stream reached");
          }
          count++;
          return value;
        } else {
          return -1;
        }
      }
    };
  }

  final protected void ack() throws IOException {
      out.write(0);
      out.flush();
  }

  final protected void readAck() throws IOException {
    int c = in.read();
    switch (c) {
      case 0:
        break;
      case 1:
        log.log(Level.FINE, "Received warning: " + readLine());
        break;
      case 2:
        throw new IOException("Received nack: " + readLine());
    }
  }

  final protected String readLine() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    while (true) {
      int c = in.read();
      if (c == '\n') {
        return baos.toString();
      }
      else if (c == -1) {
        throw new IOException("End of stream");
      }
      else {
        baos.write(c);
      }
    }
  }

  final public void start(Environment env) throws IOException {
    this.env = env;

    //
    thread = new Thread(this, "CRaSH");
    thread.start();
  }

  final public void destroy() {
    thread.interrupt();
  }

  final public void run() {
    int exitStatus = OK;
    String exitMsg = null;

    //
    try {
      execute();
    }
    catch (Exception e) {
      log.log(Level.SEVERE, "Error during command execution", e);
      exitMsg = e.getMessage();
      exitStatus = ERROR;
    }
    finally {
      // Say we are done
      if (callback != null) {
        callback.onExit(exitStatus, exitMsg);
      }
    }
  }

  private void execute() throws Exception {
    Map<String, String> properties = new HashMap<String, String>();

    // Need portal container name ?
    int pos1 = target.indexOf(':');
    String path;
    String workspaceName;
    if (pos1 != -1) {
      int pos2 = target.indexOf(':', pos1 + 1);
      if (pos2 != -1) {
        // container:workspace_name:path
        properties.put("container", target.substring(0, pos1));
        workspaceName = target.substring(pos1 + 1, pos2);
        path = target.substring(pos2 + 1);
      }
      else {
        // workspace_name:path
        workspaceName = target.substring(0, pos1);
        path = target.substring(pos1 + 1);
      }
    }
    else {
      workspaceName = null;
      path = target;
    }

    //
    Repository repository = JCRPlugin.findRepository(properties);

    // Obtain credentials from SSH
    String userName = session.getAttribute(SSHLifeCycle.USERNAME);
    String password = session.getAttribute(SSHLifeCycle.PASSWORD);
    Credentials credentials = new SimpleCredentials(userName, password.toCharArray());

    //
    Session session;
    if (workspaceName != null) {
      session = repository.login(credentials, workspaceName);
    }
    else {
      session = repository.login(credentials);
    }

    //
    try {
      execute(session, path);
    }
    finally {
      session.logout();
    }
  }

  protected abstract void execute(Session session, String path) throws Exception;

}
