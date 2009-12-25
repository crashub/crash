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
package org.crsh.connector.sshd;

import org.apache.sshd.SshServer;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.PublickeyAuthenticator;
import org.apache.sshd.server.keyprovider.PEMGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.crsh.connector.CRaSHLifeCycle;

import java.security.PublicKey;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class SSHLifeCycle extends CRaSHLifeCycle {

  /** . */
  private SshServer server;

  @Override
  protected void init() {
    String keyPath = getShellContext().getServletContext().getRealPath("/WEB-INF/sshd/hostkey.pem");

    //
    try {

      SshServer server = SshServer.setUpDefaultServer();
      server.setPort(2000);
      server.setShellFactory(new CRaSHCommandFactory(getShellBuilder()));
      server.setKeyPairProvider(new PEMGeneratorHostKeyProvider(keyPath));
      server.setPublickeyAuthenticator(new PublickeyAuthenticator() {
        public boolean authenticate(String username, PublicKey key, ServerSession session) {
          return true;
        }
      });
/*
      server.setKeyExchangeFactories(Collections.<NamedFactory<KeyExchange>>emptyList());
      server.setUserAuthFactories(Collections.<NamedFactory<UserAuth>>emptyList());
      server.setCipherFactories(Collections.<NamedFactory<Cipher>>emptyList());
      server.setCompressionFactories(Collections.<NamedFactory<Compression>>emptyList());
      server.setMacFactories(Collections.<NamedFactory<Mac>>emptyList());
      server.setChannelFactories(Collections.<NamedFactory<Channel>>emptyList());
*/
      server.setPasswordAuthenticator(new PasswordAuthenticator() {
        public boolean authenticate(String username, String password, ServerSession session) {
          return true;
        }
      });

      //
      System.out.println("About to start server");
      server.start();
      System.out.println("Server started");

      //
      this.server = server;
    }
    catch (Throwable e) {
      System.out.println("Could not start SSHD");
      e.printStackTrace();
    }
  }

  @Override
  protected void destroy() {
    if (server != null) {
      try {
        server.stop();
      }
      catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
