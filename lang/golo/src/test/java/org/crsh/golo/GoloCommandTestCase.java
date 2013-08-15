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
package org.crsh.golo;

import org.crsh.plugin.CRaSHPlugin;
import org.crsh.shell.AbstractCommandTestCase;
import org.crsh.shell.ErrorType;

import java.util.List;

/**
 * @author Julien Viet
 */
public class GoloCommandTestCase extends AbstractCommandTestCase {

  @Override
  protected List<CRaSHPlugin<?>> getPlugins() {
    List<CRaSHPlugin<?>> plugins = super.getPlugins();
    plugins.add(new GoloCommandManager());
    return plugins;
  }

  public void testCompilationFailure() {
    lifeCycle.bind("foo", "golo", "function run = { return \"pass\"; }");
    assertUnknownCommand("foo");
  }

  public void testInvoke() {
    lifeCycle.bind("foo", "golo", "function run = { return \"pass\" }");
    assertEquals("pass", assertOk("foo"));
  }

  public void testReturnObject() {
    lifeCycle.bind("foo", "golo", "function run = |arg| { return \"<\" + arg + \">\" }");
    assertEquals("<bar>", assertOk("foo bar"));
  }

  public void testFailure() {
    lifeCycle.bind("foo", "golo", "function run = { throw RuntimeException(\"w00t\") }");
    Throwable t = assertError("foo", ErrorType.EVALUATION);
    RuntimeException e = assertInstance(RuntimeException.class, t);
    assertEquals("w00t", e.getMessage());
  }

  public void testProvide() {
    lifeCycle.bind("foo", "golo", "import org.crsh.golo.CRaSH\nfunction run = { provide(\"something\") }");
    assertEquals("something", assertOk("foo"));
  }

  public void testWidth() {
    lifeCycle.bind("foo", "golo", "import org.crsh.golo.CRaSH\nfunction run = { return width() }");
    assertEquals("32", assertOk("foo"));
  }

  public void testHeight() {
    lifeCycle.bind("foo", "golo", "import org.crsh.golo.CRaSH\nfunction run = { return height() }");
    assertEquals("40", assertOk("foo"));
  }

  public void testSession() {
    lifeCycle.bind("foo", "golo", "import org.crsh.golo.CRaSH\nfunction run = { return session():get(\"abc\") }");
    shell.getSession().put("abc", "def");
    assertEquals("def", assertOk("foo"));
  }

  public void testAttributes() {
    lifeCycle.bind("foo", "golo", "import org.crsh.golo.CRaSH\nfunction run = { return attributes():get(\"abc\") }");
    shell.getAttributes().put("abc", "def");
    assertEquals("def", assertOk("foo"));
  }

  public void testWriter() {
    lifeCycle.bind("foo", "golo", "import org.crsh.golo.CRaSH\nfunction run = { context():getWriter():print(\"the_text\") }");
    assertEquals("the_text", assertOk("foo"));
  }
}
