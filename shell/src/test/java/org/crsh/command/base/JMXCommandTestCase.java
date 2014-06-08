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
package org.crsh.command.base;

import org.crsh.shell.AbstractShellTestCase;
import test.command.Commands;

import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.HashMap;

/** @author Julien Viet */
public class JMXCommandTestCase extends AbstractShellTestCase {

  /** . */
  private ObjectName OPERATING_SYSTEM;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    OPERATING_SYSTEM = new ObjectName("java.lang:type=OperatingSystem");
  }

  public void testFind() throws Exception {
    lifeCycle.bindClass("consume", Commands.ConsumeObject.class);
    Commands.list.clear();
    assertOk("jmx query java.lang:* | consume");
    assertTrue(Commands.list.contains(OPERATING_SYSTEM));
  }

  public void testGet() throws Exception {
    Object version = ManagementFactory.getPlatformMBeanServer().getAttribute(OPERATING_SYSTEM, "Version");
    lifeCycle.bindClass("consume", Commands.ConsumeObject.class);
    Commands.list.clear();
    assertOk("jmx query " + OPERATING_SYSTEM + " | jmx get -n MBean -a Version | consume");
    HashMap<String, Object> expected = new HashMap<String, Object>();
    expected.put("Version", version);
    expected.put("MBean", OPERATING_SYSTEM);
    assertEquals(Collections.<Object>singletonList(expected), Commands.list);
  }
}
