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

package org.crsh.text.ui;

import org.crsh.shell.AbstractShellTestCase;

public class EvalTestCase extends AbstractShellTestCase {


  public void testProvideRenderable() {
    String foo =
        "class foo {\n" +
        "  @Command\n" +
        "  public void main() {\n" +
        "    def builder = new org.crsh.text.ui.UIBuilder().eval {" +
        "      bar()\n" +
        "    }\n" +
        "    out << builder\n" +
        "  }\n" +
        "}";
    lifeCycle.bindGroovy("foo", foo);
    lifeCycle.bindGroovy("bar", "context.provide([a:1]);");

    //
    assertEquals(
        "a                              \n" +
        "--                             \n" +
        "1                              \n", assertOk("foo"));
  }

  public void testProvideText() {
    String foo =
        "class foo {\n" +
            "  @Command\n" +
            "  public void main() {\n" +
            "    def builder = new org.crsh.text.ui.UIBuilder().eval {" +
            "      bar()\n" +
            "    }\n" +
            "    out << builder\n" +
            "  }\n" +
            "}";
    lifeCycle.bindGroovy("foo", foo);
    lifeCycle.bindGroovy("bar", "out << 'hello';");

    //
    assertEquals("hello                          \n", assertOk("foo"));
  }

  public void testEvalCommandInEval() {
    String foo =
        "class foo {\n" +
            "  @Command\n" +
            "  public void main() {\n" +
            "    def builder = new org.crsh.text.ui.UIBuilder().eval {" +
            "      execute('echo bar')\n" +
            "    }\n" +
            "    out << builder\n" +
            "  }\n" +
            "}";
    lifeCycle.bindGroovy("foo", foo);

    //
    assertEquals("bar                            \n", assertOk("foo"));
  }

  public void testEvalCommandInCommand() {
    String foo =
        "class foo {\n" +
            "  @Command\n" +
            "  public void main() {\n" +
            "    execute('echo bar')\n" +
            "  }\n" +
            "}";
    lifeCycle.bindGroovy("foo", foo);

    //
    assertEquals("bar", assertOk("foo"));
  }

  public void testContextLeftShift() {
    String bar = "context << 'juu'\n" +
        "return null";
    String foo =
        "class foo {\n" +
            "  @Command\n" +
            "  public void main() {\n" +
            "    execute('bar')\n" +
            "  }\n" +
            "}";
    lifeCycle.bindGroovy("bar", bar);
    lifeCycle.bindGroovy("foo", foo);

    //
    assertEquals("juu", assertOk("foo"));
  }
}
