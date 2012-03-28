/*
 * Copyright (C) 2010 eXo Platform SAS.
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

import com.sun.tools.attach.VirtualMachine;
import org.crsh.term.processor.Processor;
import org.crsh.shell.impl.CRaSH;
import org.crsh.term.BaseTerm;
import org.crsh.term.Term;
import org.crsh.term.spi.jline.JLineIO;
import org.crsh.term.spi.net.TermIOServer;

import java.net.*;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Main {

  public static void main(String[] args) throws Exception {

    if (args.length > 0) {
      // Standalone
      String id = args[0];
      URL url = Main.class.getProtectionDomain().getCodeSource().getLocation();
      java.io.File f = new java.io.File(url.toURI());
      VirtualMachine vm = VirtualMachine.attach(id);

      TermIOServer server = new TermIOServer(new JLineIO(), 0);
      int port = server.bind();
      System.out.println("Bound on port " + port);

      //
      System.out.println("Loading agent");
      vm.loadAgent(f.getCanonicalPath(), "" + port);

      //
      try {
        server.accept();
        while (server.execute()) {
          //
        }
      } finally {
        vm.detach();
      }
    } else {
      final Bootstrap bootstrap = new Bootstrap(Thread.currentThread().getContextClassLoader());

      // Register shutdown hook
      Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() {
          bootstrap.shutdown();
        }
      });

      // Do bootstrap
      bootstrap.bootstrap();

      // Start crash for this command line
      Term term = new BaseTerm(new JLineIO());
      Processor processor = new Processor(term, new CRaSH(bootstrap.getContext()));

      //
      processor.run();
    }
  }
}
