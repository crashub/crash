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

import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.crsh.lang.impl.groovy.command.GroovyScriptCommand;
import org.crsh.shell.AbstractShellTestCase;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * @author Julien Viet
 */
public class ScriptReturnValueTestCase extends AbstractShellTestCase {

  public void testExplicitReturnFromDefaultScript() throws Exception {
    assertHasNoTag(null, "return 'something'");
  }

  public void testImplicitReturnFromDefaultScript() throws Exception {
    assertHasNoTag(null, "def implicit = 'something'");
  }

  public void testExplicitReturnFromCRaSHScript() throws Exception {
    CompilerConfiguration config = new CompilerConfiguration();
    config.setScriptBaseClass(GroovyScriptCommand.class.getName());
    GroovyShell shell = new GroovyShell(config);
    Script script = shell.parse("return 'something'");
    Field f = script.getClass().getDeclaredField("org_crsh_has_explicit_return");
    assertTrue(Modifier.isPublic(f.getModifiers()));
    assertTrue(Modifier.isStatic(f.getModifiers()));
  }

  public void testImplicitReturnFromCRaSHScript() throws Exception {
    assertHasNoTag(GroovyScriptCommand.class, "def implicit = 'something'");
  }

  private void assertHasNoTag(Class<? extends Script> scriptBaseClass, String scriptText) {
    CompilerConfiguration config = new CompilerConfiguration();
    config.setScriptBaseClass(scriptBaseClass != null ? scriptBaseClass.getName() : null);
    GroovyShell shell = new GroovyShell(config);
    Script script = shell.parse(scriptText);
    try {
      script.getClass().getDeclaredField("org_crsh_has_explicit_return");
      fail();
    }
    catch (NoSuchFieldException e) {
      // Ok
    }
  }
}
