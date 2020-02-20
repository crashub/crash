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

package org.crsh.spring;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.crsh.command.ShellSafety;
import org.crsh.command.ShellSafetyFactory;
import test.shell.base.BaseProcessContext;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellFactory;
import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellResponse;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.UrlResource;

import java.net.URL;

public class SpringTestCase extends TestCase {

  public void testFoo() throws Exception {

    URL xml = SpringTestCase.class.getResource("spring.xml");
    Assert.assertNotNull(xml);

    //
    GenericXmlApplicationContext context = new GenericXmlApplicationContext(new UrlResource(xml));
    context.start();

    //
    SpringBootstrap bootstrap = context.getBean(SpringBootstrap.class);

    // Test a bit
    ShellFactory factory = bootstrap.getContext().getPlugin(ShellFactory.class);
    Shell shell = factory.create(null, null, ShellSafetyFactory.getCurrentThreadShellSafety());
    assertNotNull(shell);
    ShellProcess process = shell.createProcess("foo_cmd");
    assertNotNull(process);
    BaseProcessContext pc = BaseProcessContext.create(process).execute();
    assertTrue(pc.getResponse() instanceof ShellResponse.Ok);
    String r = pc.getOutput();
    assertEquals("bar", r);
  }
}
