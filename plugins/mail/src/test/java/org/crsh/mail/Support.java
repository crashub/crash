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

import junit.framework.Assert;
import test.plugin.TestPluginLifeCycle;
import org.crsh.lang.impl.groovy.GroovyLanguageProxy;
import org.crsh.lang.impl.java.JavaLanguage;
import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.helper.SimpleMessageListenerAdapter;
import org.subethamail.smtp.server.SMTPServer;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/** @author Julien Viet */
public class Support {

  protected SMTPServer createServer(MessageHandlerFactory mhf) {
    return new SMTPServer(mhf);
  }

  protected TestPluginLifeCycle createLifeCycle() throws Exception {
    TestPluginLifeCycle test = new TestPluginLifeCycle(new MailPlugin(), new GroovyLanguageProxy(), new JavaLanguage());
    test.setProperty(MailPlugin.SMTP_HOST, "localhost");
    test.setProperty(MailPlugin.SMTP_PORT, 5000);
    test.setProperty(MailPlugin.SMTP_FROM, "foo@gmail.com");
    return test;
  }

  protected Future<Boolean> send(MailPlugin plugin) throws IOException, MessagingException {
    return plugin.send(Arrays.asList("dst@gmail.com"), "Testing Subject", "Dear Mail Crawler,"
        + "\n\n No spam to my email, please!");
  }

  protected void assertResponse(Wiser wiser, Future<Boolean> response) throws MessagingException, ExecutionException, InterruptedException {
    Assert.assertTrue(response.get());
    Assert.assertEquals(1, wiser.getMessages().size());
    WiserMessage msg = wiser.getMessages().get(0);
    assertMessage(msg);
  }

  protected void assertMessage(WiserMessage msg) throws MessagingException {
    Assert.assertEquals("foo@gmail.com", msg.getEnvelopeSender());
    Assert.assertEquals("dst@gmail.com", msg.getEnvelopeReceiver());
    Assert.assertEquals("Testing Subject", msg.getMimeMessage().getSubject());
  }

  protected void execute(TestPluginLifeCycle lifeCycle, Wiser wiser) throws IOException, MessagingException, ExecutionException, InterruptedException {
    MailPlugin plugin = lifeCycle.getContext().getPlugin(MailPlugin.class);
    Future<Boolean> future = send(plugin);
    assertResponse(wiser, future);
  }

  public void doTest() throws Exception {
    Wiser wiser = new Wiser();
    SMTPServer smtpServer = createServer(new SimpleMessageListenerAdapter(wiser));
    smtpServer.setHostName("localhost");
    smtpServer.setPort(5000);
    TestPluginLifeCycle lifeCycle = createLifeCycle();
    try {
      lifeCycle.start();
      smtpServer.start();
      execute(lifeCycle, wiser);
    }
    finally {
      smtpServer.stop();
      lifeCycle.stop();
    }
  }
}
