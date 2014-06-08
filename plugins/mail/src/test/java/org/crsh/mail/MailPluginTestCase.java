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
package org.crsh.mail;

import junit.framework.TestCase;
import test.plugin.TestPluginLifeCycle;
import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.auth.LoginAuthenticationHandlerFactory;
import org.subethamail.smtp.auth.LoginFailedException;
import org.subethamail.smtp.auth.UsernamePasswordValidator;
import org.subethamail.smtp.server.SMTPServer;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/** @author Julien Viet */
public class MailPluginTestCase extends TestCase {

  public void testSendPlain() throws Exception {
    Support support = new Support();
    support.doTest();
  }

  public void testSendHtml() throws Exception {
    Support support = new Support() {
      @Override
      protected Future<Boolean> send(MailPlugin plugin) throws IOException, MessagingException {
        return plugin.send(Arrays.asList("dst@gmail.com"), "Testing Subject", "<html><body>hello wolrd</body></html>", "text/html;charset=UTF-8");
      }

    };
    support.doTest();
  }

  public void testAuth() throws Exception {
    final AtomicReference<String> usernameRef = new AtomicReference<String>();
    final AtomicReference<String> passwordRef = new AtomicReference<String>();
    Support support = new Support() {
      @Override
      protected TestPluginLifeCycle createLifeCycle() throws Exception {
        TestPluginLifeCycle lifeCycle = super.createLifeCycle();
        lifeCycle.setProperty(MailPlugin.SMTP_USERNAME, "foo");
        lifeCycle.setProperty(MailPlugin.SMTP_PASSWORD, "bar");
        return lifeCycle;
      }

      @Override
      protected SMTPServer createServer(MessageHandlerFactory mhf) {
        SMTPServer smtpServer = super.createServer(mhf);
        smtpServer.setAuthenticationHandlerFactory(new LoginAuthenticationHandlerFactory(new UsernamePasswordValidator() {
          public void login(String username, String password) throws LoginFailedException {
            usernameRef.set(username);
            passwordRef.set(password);
          }
        }));
        return smtpServer;
      }
    };
    support.doTest();
    assertEquals("foo", usernameRef.get());
    assertEquals("bar", passwordRef.get());
  }

  public void testAuthFailed() throws Exception {
    final AtomicBoolean done = new AtomicBoolean();
    Support support = new Support() {
      @Override
      protected TestPluginLifeCycle createLifeCycle() throws Exception {
        TestPluginLifeCycle lifeCycle = super.createLifeCycle();
        lifeCycle.setProperty(MailPlugin.SMTP_USERNAME, "foo");
        lifeCycle.setProperty(MailPlugin.SMTP_PASSWORD, "bar");
        return lifeCycle;
      }
      @Override
      protected SMTPServer createServer(MessageHandlerFactory mhf) {
        SMTPServer smtpServer = super.createServer(mhf);
        smtpServer.setAuthenticationHandlerFactory(new LoginAuthenticationHandlerFactory(new UsernamePasswordValidator() {
          public void login(String username, String password) throws LoginFailedException {
            done.set(true);
            throw new LoginFailedException();
          }
        }));
        return smtpServer;
      }
      @Override
      protected void assertResponse(Wiser wiser, Future<Boolean> response) throws MessagingException, ExecutionException, InterruptedException {
        assertFalse(response.get());
      }
    };
    support.doTest();
    assertEquals(true, done.get());
  }

  public void testSendTLS() throws Exception {

    //
    File keyStore = new File(MailPluginTestCase.class.getResource("keystore.jks").toURI());
    File trustStore = new File(MailPluginTestCase.class.getResource("truststore.jks").toURI());
    Properties oldProps = new Properties(System.getProperties());
    System.setProperty("javax.net.ssl.keyStore", keyStore.getAbsolutePath());
    System.setProperty("javax.net.ssl.trustStore", trustStore.getAbsolutePath());
    System.setProperty("javax.net.ssl.keyStorePassword", "crashub");

    //
    try {
      char[] keyStorePassphrase = "crashub".toCharArray();
      KeyStore ksKeys = KeyStore.getInstance("JKS");
      ksKeys.load(new FileInputStream(keyStore), keyStorePassphrase);
      KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
      kmf.init(ksKeys, keyStorePassphrase);
      char[] trustStorePassphrase = "crashub".toCharArray();
      KeyStore ksTrust = KeyStore.getInstance("JKS");
      ksTrust.load(new FileInputStream(trustStore), trustStorePassphrase);
      TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
      tmf.init(ksTrust);
      final SSLContext sslContext = SSLContext.getInstance("TLS");
      sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

      //
      Support support = new Support() {
        @Override
        protected SMTPServer createServer(MessageHandlerFactory mhf) {
          return new SMTPServer(mhf) {
            @Override
            public SSLSocket createSSLSocket(Socket socket) throws IOException {
              InetSocketAddress remoteAddress = (InetSocketAddress) socket.getRemoteSocketAddress();
              SSLSocketFactory sf = sslContext.getSocketFactory();
              SSLSocket s = (SSLSocket) (sf.createSocket(socket, remoteAddress.getHostName(), socket.getPort(), true));
              s.setUseClientMode(false);
              s.setEnabledProtocols(StrongTLS.intersection(s.getSupportedProtocols(), StrongTLS.ENABLED_PROTOCOLS));
              s.setEnabledCipherSuites(StrongTLS.intersection(s.getSupportedCipherSuites(), StrongTLS.ENABLED_CIPHER_SUITES));
              return s;
            }
          };
        }

        @Override
        protected TestPluginLifeCycle createLifeCycle() throws Exception {
          TestPluginLifeCycle lifeCycle = super.createLifeCycle();
          lifeCycle.setProperty(MailPlugin.SMTP_SECURE, SmtpSecure.TLS);
          return lifeCycle;
        }
      };

      //
      support.doTest();
    }
    finally {
      System.setProperty("javax.net.ssl.keyStore", oldProps.getProperty("javax.net.ssl.keyStore"));
      System.setProperty("javax.net.ssl.trustStore", oldProps.getProperty("javax.net.ssl.trustStore"));
      System.setProperty("javax.net.ssl.keyStorePassword", oldProps.getProperty("javax.net.ssl.keyStorePassword"));
    }
  }

  public void testSendAttachment() throws Exception {
    final File f = new File(MailPluginTestCase.class.getResource("image.png").toURI());
    Support support = new Support() {
      @Override
      protected Future<Boolean> send(MailPlugin plugin) throws IOException, MessagingException {
        return plugin.send(Arrays.asList("dst@gmail.com"), "Testing Subject", "Dear Mail Crawler,"
            + "\n\n No spam to my email, please!", new FileDataSource(f));
      }
      @Override
      protected void assertMessage(WiserMessage msg) throws MessagingException {
        super.assertMessage(msg);
        String data = new String(msg.getData());
        String match = "name=image.png";
        assertTrue("Was expecting " + data + " to contain <" + match + ">", data.contains(match));
      }
    };
    support.doTest();
  }
}
