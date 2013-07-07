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
import org.crsh.AbstractTestCase;
import org.crsh.BaseProcessContext;
import org.crsh.TestPluginLifeCycle;
import org.crsh.shell.ShellResponse;
import org.crsh.shell.impl.command.CRaSHSession;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/** @author Julien Viet */
public class MailCommandTestCase extends AbstractTestCase {
  
  
  public void testFoo() throws Exception {

    Support support = new Support() {
      @Override
      protected void execute(TestPluginLifeCycle lifeCycle, Wiser wiser) throws IOException, MessagingException, ExecutionException, InterruptedException {
        CRaSHSession shell = lifeCycle.createShell();
        BaseProcessContext process = BaseProcessContext.create(shell, "echo abc | mail -s the_subject -b admin@gmail.com").execute();
        ShellResponse.Ok ok = assertInstance(ShellResponse.Ok.class, process.getResponse());
        Assert.assertEquals(1, wiser.getMessages().size());
        WiserMessage msg = wiser.getMessages().get(0);
        // Assert.assertEquals("foo@gmail.com", msg.getEnvelopeSender());
        Assert.assertEquals("admin@gmail.com", msg.getEnvelopeReceiver());
        Assert.assertEquals("the_subject", msg.getMimeMessage().getSubject());
        String data = new String(msg.getData());
        assertTrue(data.contains("Content-Type: text/html;charset=UTF-8"));
      }
    };

    support.doTest();


  }
}
