/*
 * Copyright (C) 2011 eXo Platform SAS.
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
package org.crsh.standalone;

import org.crsh.term.processor.Processor;
import org.crsh.shell.impl.CRaSH;
import org.crsh.term.BaseTerm;
import org.crsh.term.Term;
import org.crsh.term.spi.net.TermIOClient;

import java.io.Closeable;
import java.lang.instrument.Instrumentation;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class Agent {

  public static void agentmain(final String agentArgs, Instrumentation inst) throws Exception {
    System.out.println("agent loaded");

    Thread t = new Thread() {
      @Override
      public void run() {
        try {
          int port = Integer.parseInt(agentArgs);

          final Bootstrap bootstrap = new Bootstrap(Thread.currentThread().getContextClassLoader());

          // Do bootstrap
          bootstrap.bootstrap();

          //
          final TermIOClient client = new TermIOClient(port);
          System.out.println("connecting to server port " + port);
          client.connect();

          //
          Term term = new BaseTerm(client);
          Processor processor = new Processor(term, new CRaSH(bootstrap.getContext()));
          processor.addListener(new Closeable() {
            public void close() {
              client.close();
            }
          });
          processor.run();

          // End for now
          bootstrap.shutdown();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    };
    t.start();
  }
}
