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
import org.crsh.cmdline.binding.MethodArgumentBinding;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class DescriptionTestCase extends TestCase {

  public void testNoDescription() throws Exception {

    class A { }

    CommandDescriptor<A, ?> c = CommandDescriptor.create(A.class);
    assertEquals("", c.getDescription());
    assertEquals(null, c.getInfo());
  }

  public void testClassDescription() throws Exception {

    @Description(display = "class_display", usage = "class_usage", man = "class_man")
    class A { }

    CommandDescriptor<A, ?> c = CommandDescriptor.create(A.class);
    assertEquals("class_display", c.getDescription());
    assertEquals("class_display", c.getInfo().getDisplay());
    assertEquals("class_usage", c.getInfo().getUsage());
    assertEquals("class_man", c.getInfo().getMan());
  }

  public void testMethodDescription() throws Exception {

    class A {
      @Description(display = "method_display", usage = "method_usage", man = "method_man")
      @Command void m() {}
    }

    ClassDescriptor<A> c = CommandDescriptor.create(A.class);
    MethodDescriptor<A> m = c.getMethod("m");
    assertEquals("method_display", m.getDescription());
    assertEquals("method_display", m.getInfo().getDisplay());
    assertEquals("method_usage", m.getInfo().getUsage());
    assertEquals("method_man", m.getInfo().getMan());
  }

  public void testParameterDescription() throws Exception {

    class A {
      @Command void m(@Description(display = "option_display", usage = "option_usage", man = "option_man") @Option(names = "a") String s) {}
    }

    ClassDescriptor<A> c = CommandDescriptor.create(A.class);
    MethodDescriptor<A> m = c.getMethod("m");
    OptionDescriptor<MethodArgumentBinding> a = m.getOption("-a");
    assertEquals("option_display", a.getDescription());
    assertEquals("option_display", a.getInfo().getDisplay());
    assertEquals("option_usage", a.getInfo().getUsage());
    assertEquals("option_man", a.getInfo().getMan());
  }

  @Option(names = "a")
  @Description(display = "foo_display", usage = "foo_usage", man = "foo_man")
  @Retention(RetentionPolicy.RUNTIME)
  public static @interface Foo { }

  @Option(names = "a")
  @Retention(RetentionPolicy.RUNTIME)
  public static @interface Bar { }

  public void testParameterMetaDescription1() throws Exception {

    class A {
      @Command void m(@Foo String s) {}
    }

    ClassDescriptor<A> c = CommandDescriptor.create(A.class);
    MethodDescriptor<A> m = c.getMethod("m");
    OptionDescriptor<MethodArgumentBinding> a = m.getOption("-a");
    assertEquals("foo_display", a.getDescription());
    assertEquals("foo_display", a.getInfo().getDisplay());
    assertEquals("foo_usage", a.getInfo().getUsage());
    assertEquals("foo_man", a.getInfo().getMan());
  }

  public void testParameterMetaDescription2() throws Exception {

    class A {
      @Command void m(@Bar String s) {}
    }

    ClassDescriptor<A> c = CommandDescriptor.create(A.class);
    MethodDescriptor<A> m = c.getMethod("m");
    OptionDescriptor<MethodArgumentBinding> a = m.getOption("-a");
    assertEquals("", a.getDescription());
    assertEquals(null, a.getInfo());
  }

  public void testParameterMetaDescription3() throws Exception {

    class A {
      @Command void m(@Description(display = "option_display") @Foo String s) {}
    }

    ClassDescriptor<A> c = CommandDescriptor.create(A.class);
    MethodDescriptor<A> m = c.getMethod("m");
    OptionDescriptor<MethodArgumentBinding> a = m.getOption("-a");
    assertEquals("option_display", a.getDescription());
    assertEquals("option_display", a.getInfo().getDisplay());
    assertEquals("foo_usage", a.getInfo().getUsage());
    assertEquals("foo_man", a.getInfo().getMan());
  }
}
