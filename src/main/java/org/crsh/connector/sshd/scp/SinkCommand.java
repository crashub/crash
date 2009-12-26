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
import org.crsh.util.IO;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class SinkCommand extends SCPCommand implements Runnable {

  /** . */
  private final String target;

  /** . */
  private Environment env;

  /** . */
  private Thread thread;

  /** . */
  private boolean recursive;

  public SinkCommand(String target, boolean recursive) {
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

    FileSystem fs = new FileSystem() {
      public void startDirectory(String directoryName) throws IOException {
        System.out.println("Dir " + directoryName);
      }

      public void file(String fileName, int length, InputStream data) throws IOException {
        System.out.println("File " + fileName);
        byte[] bytes = IO.readAsBytes(data);
      }


      public void endDirectory(String directoryName) throws IOException {
        System.out.println("Dir " + directoryName);
      }
    };

    //
    int exitStatus = OK;
    String exitMsg = null;

    try {
      System.out.println("About to begin import");
      FileSystemAction.read(this, fs);
      System.out.println("Import finished");
    }
    catch (IOException e) {
      exitMsg = "Cannot import properly";
      exitStatus = ERROR;
    }
    finally {
      // Say we are done
      if (callback != null) {
        callback.onExit(exitStatus, exitMsg);
      }
    }
  }
}
