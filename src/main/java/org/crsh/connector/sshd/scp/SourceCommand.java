/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.crsh.connector.sshd.scp;

import org.apache.sshd.server.Environment;
import org.crsh.jcr.JCR;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.Workspace;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class SourceCommand extends SCPCommand implements Runnable {

  /** . */
  private final String target;

  /** . */
  private Environment env;

  /** . */
  private Thread thread;

  /** . */
  private boolean recursive;

  public SourceCommand(String target, boolean recursive) {
    this.target = target;
    this.recursive = recursive;
  }

  public void start(Environment env) throws IOException {
    this.env = env;

    //
    thread = new Thread(this, "CRaSH");
    thread.start();
  }

  public void destroy() {
    thread.interrupt();
  }

  public void run() {
    try {
      execute();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void execute() throws Exception {
    Map<String, String> properties = new HashMap<String, String>();

    //
    int exitStatus = OK;
    String exitMsg = null;

    // Need portal container name ?
    int pos1 = target.indexOf(':');
    String path;
    String workspaceName;
    if (pos1 != -1) {
      int pos2 = target.indexOf(':', pos1 + 1);
      if (pos2 != -1) {
        // container_name:workspace_name:path
        properties.put("exo.container.name", target.substring(0, pos1));
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
    Repository repository = JCR.getRepository(properties);

    //
    System.out.println("Obtained repository " + repository);

    //
    Session session;
    if (workspaceName != null) {
      session = repository.login(workspaceName);
    }
    else {
      session = repository.login();
    }

    //
    System.out.println("Connected to session " + session);

    //
    try {
      Item item = session.getItem(path);
      if (item instanceof Node) {
        Exporter exporter = new Exporter(this);
        session.exportDocumentView(path, exporter, false, false);
      } else {
        exitMsg = "Cannot export property";
        exitStatus = ERROR;
      }
    }
    finally {
      // Say we are done
      if (callback != null) {
        callback.onExit(exitStatus, exitMsg);
      }
      session.logout();
    }
  }
}
