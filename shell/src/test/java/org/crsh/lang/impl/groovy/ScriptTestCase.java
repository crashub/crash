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
package org.crsh.lang.impl.groovy;

import org.crsh.shell.AbstractShellTestCase;
import test.command.Commands;

/** @author Julien Viet */
public class ScriptTestCase extends AbstractShellTestCase {

  public static boolean failed;

  public void testCommandExecutionFailureThrowsException() throws Exception {
    String script =
        "try {\n" +
        "thrower();\n" +
        "}\n" +
        "catch(javax.naming.NamingException e) {\n" +
        "e.printStackTrace();\n" +
        "org.crsh.lang.impl.groovy.ScriptTestCase.failed = true\n" +
        "}";
    lifeCycle.bindClass("thrower", Commands.ThrowCheckedException.class);
    lifeCycle.bindGroovy("script", script);
    failed = false;
    assertOk("script");
    assertTrue(failed);
  }
}
