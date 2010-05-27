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
package org.crsh.term.sshd;

import org.apache.sshd.common.PtyMode;
import org.apache.sshd.server.Environment;
import org.crsh.shell.connector.Connector;
import org.crsh.shell.ShellResponse;
import org.crsh.shell.impl.CRaSH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.concurrent.Future;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class CRaSHCommand extends AbstractCommand implements Runnable {

  /** . */
  private final Logger log = LoggerFactory.getLogger(getClass());

  /** . */
  private final CRaSHCommandFactory factory;

  /** . */
  private Thread thread;

  public CRaSHCommand(CRaSHCommandFactory factory) {
    this.factory = factory;
  }

  /** . */
  private SSHContext context;

  /** . */
  private Connector connector;

  /** . */
  private CRaSH shell;

  public void start(Environment env) throws IOException {
    context = new SSHContext(env.getPtyModes().get(PtyMode.VERASE));
    shell = factory.builder.build();
    connector = new Connector(factory.executor, shell);

    //
    thread = new Thread(this, "CRaSH");
    thread.start();
  }

  public void destroy() {
    connector.close();
    thread.interrupt();
  }

  public void run() {
    try {
      OutputStreamWriter writer = new OutputStreamWriter(out);
      SSHReader reader = new SSHReader(new InputStreamReader(in), context.verase, writer);

      //
      String welcome = connector.open();
      writer.write(welcome);
      writer.flush();

      //
      while (true) {
        String request = reader.nextLine();

        //
        if (request == null) {
          break;
        }

        //
        Future<ShellResponse> futureResponse = connector.submitEvaluation(request);

        //
        ShellResponse response = futureResponse.get();

        //
        writer.write(response.getText());
//        writer.write(connector.getPrompt());
        writer.flush();

        //
        if (response instanceof ShellResponse.Close) {
          connector.close();
          shell.close();
          break;
        }
      }
    }
    catch (Exception e) {
      log.error("Error when executing command", e);
    }
    finally {
      callback.onExit(0);
    }
  }
}


