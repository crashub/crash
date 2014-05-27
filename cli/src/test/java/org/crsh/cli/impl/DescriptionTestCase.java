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

package org.crsh.cli.impl;

import org.crsh.cli.Named;
import org.crsh.cli.descriptor.CommandDescriptor;
import org.crsh.cli.descriptor.Description;
import org.crsh.cli.descriptor.OptionDescriptor;
import junit.framework.TestCase;
import org.crsh.cli.Command;
import org.crsh.cli.Man;
import org.crsh.cli.Option;
import org.crsh.cli.Usage;
import org.crsh.cli.impl.descriptor.IntrospectionException;
import org.crsh.cli.impl.lang.CommandFactory;
import org.crsh.cli.impl.lang.Instance;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class DescriptionTestCase extends TestCase {

  public void testNoDescription() throws Exception {

    class A { }

    CommandDescriptor<Instance<A>> c = CommandFactory.DEFAULT.create(A.class);
    assertEquals("", c.getUsage());
    assertEquals(new Description(), c.getDescription());
  }

  public void testClassDescription() throws Exception {

    @Usage("class_usage")
    @Man("class_man")
    class A { }

    CommandDescriptor<Instance<A>> c = CommandFactory.DEFAULT.create(A.class);
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

    CommandDescriptor<Instance<A>> c = CommandFactory.DEFAULT.create(A.class);
    CommandDescriptor<Instance<A>> m = c.getSubordinate("m");
    assertEquals("method_usage", m.getUsage());
    assertEquals("method_usage", m.getDescription().getUsage());
    assertEquals("method_man", m.getDescription().getMan());
  }

  public void testClassNameOverride() throws Exception {
    @Named("foo") class A { @Command public void main() {} }
    CommandDescriptor<Instance<A>> c = CommandFactory.DEFAULT.create(A.class);
    assertEquals("foo", c.getName());
  }

  public void testMethodNameOverride() throws Exception {
    class A { @Named("foo") @Command public void bar() {} }
    CommandDescriptor<Instance<A>> c = CommandFactory.DEFAULT.create(A.class);
    assertEquals("A", c.getName());
    assertNotNull(c.getSubordinate("foo"));
    assertEquals("foo", c.getSubordinate("foo").getName());
  }

  public void testInvalidName() {
    @Named("") class A { @Command public void main() {} }
    @Named(" ") class B { @Command public void main() {} }
    @Named("0a") class C { @Command public void main() {} }
    @Named("a)") class D { @Command public void main() {} }
    for (Class<?> clazz : new Class[]{A.class,B.class,C.class,D.class}) {
      try {
        CommandFactory.DEFAULT.create(clazz);
        fail();
      }
      catch (IntrospectionException ignore) {
      }
    }
  }

  public void testParameterDescription() throws Exception {

    class A {
      @Command void m(
        @Usage("option_usage")
        @Man("option_man")
        @Option(names = "a") String s) {}
    }

    CommandDescriptor<Instance<A>> c = CommandFactory.DEFAULT.create(A.class);
    CommandDescriptor<Instance<A>> m = c.getSubordinate("m");
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

    CommandDescriptor<Instance<A>> c = CommandFactory.DEFAULT.create(A.class);
    CommandDescriptor<Instance<A>> m = c.getSubordinate("m");
    OptionDescriptor a = m.getOption("-a");
    assertEquals("foo_usage", a.getUsage());
    assertEquals("foo_usage", a.getDescription().getUsage());
    assertEquals("foo_man", a.getDescription().getMan());
  }

  public void testParameterMetaDescription2() throws Exception {

    class A {
      @Command void m(@Bar String s) {}
    }

    CommandDescriptor<Instance<A>> c = CommandFactory.DEFAULT.create(A.class);
    CommandDescriptor<Instance<A>> m = c.getSubordinate("m");
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

    CommandDescriptor<Instance<A>> c = CommandFactory.DEFAULT.create(A.class);
    CommandDescriptor<Instance<A>> m = c.getSubordinate("m");
    OptionDescriptor a = m.getOption("-a");
    assertEquals("option_usage", a.getUsage());
    assertEquals("option_usage", a.getDescription().getUsage());
    assertEquals("foo_man", a.getDescription().getMan());
  }
}
