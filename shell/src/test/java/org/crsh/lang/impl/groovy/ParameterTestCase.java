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

public class ParameterTestCase extends AbstractShellTestCase {

  /** . */
  private final String option_command = "class option_command {\n" +
      "@Command\n" +
      "public String main(@Option(names=['o','option']) String opt) {\n" +
      "return opt;" +
      "}\n" +
      "}";

  public void testShortOption() throws Exception {

    lifeCycle.bindGroovy("option_command", option_command);

    assertEquals("bar", assertOk("option_command -o bar"));

    //
    String foo = "class foo {\n" +
        "@Command\n" +
        "public String main() {\n" +
        "option_command o:'bar'\n" +
        "}\n" +
        "}";
    lifeCycle.bindGroovy("foo", foo);
    lifeCycle.bindGroovy("option_command", option_command);

    //
    assertEquals("bar", assertOk("foo"));
  }

  public void testShortOptionInScript() throws Exception {
    String foo = "option_command o:'bar'\n";
    lifeCycle.bindGroovy("foo", foo);
    lifeCycle.bindGroovy("option_command", option_command);

    //
    assertEquals("bar", assertOk("foo"));
  }

  public void testLongOption() throws Exception {
    String foo = "class foo {\n" +
        "@Command\n" +
        "public String main() {\n" +
        "option_command option:'bar'\n" +
        "}\n" +
        "}";
    lifeCycle.bindGroovy("foo", foo);
    lifeCycle.bindGroovy("option_command", option_command);

    //
    assertEquals("bar", assertOk("foo"));
  }

  public void testLongOptionInScript() throws Exception {
    String foo = "option_command option:'bar'\n";
    lifeCycle.bindGroovy("foo", foo);
    lifeCycle.bindGroovy("option_command", option_command);

    //
    assertEquals("bar", assertOk("foo"));
  }
}
