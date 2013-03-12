/*
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.crsh.cmdline;

import junit.framework.TestCase;
import org.crsh.cmdline.annotations.Command;
import org.crsh.cmdline.annotations.Man;
import org.crsh.cmdline.annotations.Option;
import org.crsh.cmdline.annotations.Usage;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class DescriptionTestCase extends TestCase {

  public void testNoDescription() throws Exception {

    class A { }

    CommandDescriptor<A> c = CommandFactory.DEFAULT.create(A.class);
    assertEquals("", c.getUsage());
    assertEquals(new Description(), c.getDescription());
  }

  public void testClassDescription() throws Exception {

    @Usage("class_usage")
    @Man("class_man")
    class A { }

    CommandDescriptor<A> c = CommandFactory.DEFAULT.create(A.class);
    assertEquals("class_usage", c.getUsage());
    assertEquals("class_usage", c.getDescription().getUsage());
    assertEquals("class_man", c.getDescription().getMan());
  }

  public void testMethodDescription() throws Exception {

    class A {
      @Usage("method_usage")
      @Man("method_man")
      @Command void m() {}
    }

    CommandDescriptor<A> c = CommandFactory.DEFAULT.create(A.class);
    CommandDescriptor<A> m = c.getSubordinate("m");
    assertEquals("method_usage", m.getUsage());
    assertEquals("method_usage", m.getDescription().getUsage());
    assertEquals("method_man", m.getDescription().getMan());
  }

  public void testParameterDescription() throws Exception {

    class A {
      @Command void m(
        @Usage("option_usage")
        @Man("option_man")
        @Option(names = "a") String s) {}
    }

    CommandDescriptor<A> c = CommandFactory.DEFAULT.create(A.class);
    CommandDescriptor<A> m = c.getSubordinate("m");
    OptionDescriptor a = m.getOption("-a");
    assertEquals("option_usage", a.getUsage());
    assertEquals("option_usage", a.getDescription().getUsage());
    assertEquals("option_man", a.getDescription().getMan());
  }

  @Option(names = "a")
  @Usage("foo_usage")
  @Man("foo_man")
  @Retention(RetentionPolicy.RUNTIME)
  public static @interface Foo { }

  @Option(names = "a")
  @Retention(RetentionPolicy.RUNTIME)
  public static @interface Bar { }

  public void testParameterMetaDescription1() throws Exception {

    class A {
      @Command
      void m(@Foo String s) {}
    }

    CommandDescriptor<A> c = CommandFactory.DEFAULT.create(A.class);
    CommandDescriptor<A> m = c.getSubordinate("m");
    OptionDescriptor a = m.getOption("-a");
    assertEquals("foo_usage", a.getUsage());
    assertEquals("foo_usage", a.getDescription().getUsage());
    assertEquals("foo_man", a.getDescription().getMan());
  }

  public void testParameterMetaDescription2() throws Exception {

    class A {
      @Command void m(@Bar String s) {}
    }

    CommandDescriptor<A> c = CommandFactory.DEFAULT.create(A.class);
    CommandDescriptor<A> m = c.getSubordinate("m");
    OptionDescriptor a = m.getOption("-a");
    assertEquals("", a.getUsage());
    assertEquals(new Description(), a.getDescription());
  }

  public void testParameterMetaDescription3() throws Exception {

    class A {
      @Command void m(
        @Usage("option_usage")
        @Foo String s) {}
    }

    CommandDescriptor<A> c = CommandFactory.DEFAULT.create(A.class);
    CommandDescriptor<A> m = c.getSubordinate("m");
    OptionDescriptor a = m.getOption("-a");
    assertEquals("option_usage", a.getUsage());
    assertEquals("option_usage", a.getDescription().getUsage());
    assertEquals("foo_man", a.getDescription().getMan());
  }
}
