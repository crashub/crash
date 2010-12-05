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
package org.crsh.term.spi.sshd;

import org.apache.sshd.common.PtyMode;
import org.apache.sshd.server.Environment;
import org.crsh.shell.connector.Connector;
import org.crsh.shell.impl.CRaSH;
import org.crsh.term.BaseTerm;
import org.crsh.term.TermShellAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;

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
    shell.close();
    thread.interrupt();
  }

  public void run() {

    try {
      OutputStreamWriter writer = new OutputStreamWriter(out);
      Reader reader = new InputStreamReader(in);
      SSHIO io = new SSHIO(reader, writer, context.verase);
      BaseTerm term = new BaseTerm(io, new TermShellAdapter(connector));
      term.run();
    } finally {
      callback.onExit(0);
    }
  }
}


