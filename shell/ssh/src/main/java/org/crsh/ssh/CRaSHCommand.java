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
package org.crsh.ssh;

import org.apache.sshd.common.PtyMode;
import org.apache.sshd.server.Environment;
import org.crsh.Processor;
import org.crsh.ProcessorListener;
import org.crsh.shell.Shell;
import org.crsh.shell.concurrent.AsyncShell;
import org.crsh.shell.impl.CRaSH;
import org.crsh.term.BaseTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

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
  private SSHIO io;

  /** . */
  private Processor processor;

  public void start(Environment env) throws IOException {
    final CRaSH shell = factory.builder.build();
    final AsyncShell asyncShell = new AsyncShell(factory.executor, shell);

    //
    context = new SSHContext(env.getPtyModes().get(PtyMode.VERASE));
    io = new SSHIO(this, context.verase);
    processor = new Processor(new BaseTerm(io), asyncShell);

    //
    processor.addListener(new ProcessorListener() {
      public void closed() {
        io.close();
      }
    });

    //
    processor.addListener(new ProcessorListener() {
      public void closed() {
        asyncShell.close();
      }
    });

    //
    processor.addListener(new ProcessorListener() {
      public void closed() {
        shell.close();
      }
    });

    //
    thread = new Thread(this, "CRaSH");
    thread.start();
  }

  public void destroy() {
    io.closed.set(true);
    thread.interrupt();
  }

  public void run() {
    try {
      processor.run();
    } finally {
      callback.onExit(0);
    }
  }
}


