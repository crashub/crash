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
package org.crsh.lang.java;

import org.crsh.cli.impl.completion.CompletionMatch;
import org.crsh.cli.spi.Completion;
import org.crsh.plugin.CRaSHPlugin;
import org.crsh.shell.AbstractCommandTestCase;

import java.util.List;
import java.util.Map;

/** @author Julien Viet */
public class JavaCommandTestCase extends AbstractCommandTestCase {

  public void testSimple() throws Exception {
    assertEquals("hello", assertOk("java_command"));
    assertEquals("def", groovyShell.getVariable("abc"));
  }

  public void testCompilationFailure() {
    lifeCycle.bindJava("foo", "public class foo extends BaseCommand {}");
    assertUnknownCommand("foo");
  }

  public void testComplete() {
    CompletionMatch match = assertComplete("java_");
    Completion completion = match.getValue();
    assertEquals(1, completion.getSize());
    Map.Entry<String, Boolean> entry = completion.iterator().next();
    assertEquals("command", entry.getKey());
    assertTrue(entry.getValue());
  }
}
