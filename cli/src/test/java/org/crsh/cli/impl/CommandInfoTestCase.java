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

import org.crsh.cli.descriptor.ArgumentDescriptor;
import org.crsh.cli.descriptor.CommandDescriptor;
import org.crsh.cli.descriptor.OptionDescriptor;
import junit.framework.TestCase;
import org.crsh.cli.Argument;
import org.crsh.cli.Command;
import org.crsh.cli.Option;
import org.crsh.cli.impl.descriptor.IntrospectionException;
import org.crsh.cli.impl.lang.Instance;
import org.crsh.cli.type.ValueType;
import org.crsh.cli.impl.lang.CommandFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class CommandInfoTestCase extends TestCase {

  public void testCommandImplicitDescription() throws IntrospectionException {
    class A {
    }
    CommandDescriptor<Instance<A>> c = CommandFactory.DEFAULT.create(A.class);
    assertEquals("", c.getUsage());
    assertEquals(0, c.getArguments().size());
    assertEquals(0, c.getOptions().size());
  }

  public void testCommandDescription() throws IntrospectionException {
    class A {
    }
    CommandDescriptor<Instance<A>> c = CommandFactory.DEFAULT.create(A.class);
    assertEquals("", c.getUsage());
    assertEquals(0, c.getArguments().size());
    assertEquals(0, c.getOptions().size());
  }

  public void testOption() throws IntrospectionException {
    class A {
      @Option(names = "i")
      private int i;
    }
    CommandDescriptor<Instance<A>> ai = CommandFactory.DEFAULT.create(A.class);
    assertEquals(1,ai.getOptions().size());
    OptionDescriptor i = ai.getOption("-i");
    assertEquals(Arrays.asList("i"),i.getNames());
  }

  public void testOptionWithUpperCase() throws IntrospectionException {
    class A {
      @Option(names = "I")
      private int i;
    }
    CommandDescriptor<Instance<A>> ai = CommandFactory.DEFAULT.create(A.class);
    assertEquals(1,ai.getOptions().size());
    OptionDescriptor i = ai.getOption("-I");
    assertEquals(Arrays.asList("I"),i.getNames());
  }

  public void testArgument1() throws IntrospectionException {
    class A {
      @Argument()
      private int i;
    }
    CommandDescriptor<Instance<A>> c = CommandFactory.DEFAULT.create(A.class);
    assertEquals(1, c.getArguments().size());
    ArgumentDescriptor i = c.getArguments().get(0);
    assertEquals(ValueType.INTEGER, i.getType());
    assertEquals(Multiplicity.SINGLE, i.getMultiplicity());
    assertEquals(false, i.isRequired());
  }

  public void testArgument2() throws IntrospectionException {
    class A {
      @Argument
      private int i;
      @Argument
      private List<Integer> j;
    }
    CommandDescriptor<Instance<A>> c = CommandFactory.DEFAULT.create(A.class);
    assertEquals(2, c.getArguments().size());
    ArgumentDescriptor i = c.getArguments().get(0);
    assertEquals(ValueType.INTEGER, i.getType());
    assertEquals(Multiplicity.SINGLE, i.getMultiplicity());
    assertEquals(false, i.isRequired());
    ArgumentDescriptor j = c.getArguments().get(1);
    assertEquals(ValueType.INTEGER, j.getType());
    assertEquals(Multiplicity.MULTI, j.getMultiplicity());
  }

  public void testArgument3() throws IntrospectionException {
    class A {
      @Argument
      private List<Integer> i;
      @Argument
      private List<Integer> j;
    }
    try {
      CommandFactory.DEFAULT.create(A.class);
      fail();
    }
    catch (IntrospectionException e) {
    }
  }

  public void testMain() throws IntrospectionException {

    class A {
      @Command
      void b() {
      }
    }

    CommandDescriptor<Instance<A>> a = CommandFactory.DEFAULT.create(A.class);
    assertNotNull(a);
  }

  public void testSub() throws IntrospectionException {

    class A {
      @Command
      void b() {
      }
      @Command
      void c() {
      }
    }

    CommandDescriptor<Instance<A>> a = CommandFactory.DEFAULT.create(A.class);
    CommandDescriptor<?> b = a.getSubordinate("b");
    assertNotNull(b);

  }

  public void testOverlappingOption() throws IntrospectionException {

    class A {
      @Option(names = "a")
      String a;
      @Command
      void b(@Option(names = "a") String a) {
      }
    }

    try {
      CommandFactory.DEFAULT.create(A.class);
      fail();
    }
    catch (IntrospectionException e) {
    }
  }

  @Target({ElementType.FIELD})
  @Retention(RetentionPolicy.RUNTIME)
  @Option(names="l")
  @interface Level {
  }

  public void testAnnotation() throws IntrospectionException {

    class A {
      @Level
      String l;
    }

    CommandDescriptor<Instance<A>> a = CommandFactory.DEFAULT.create(A.class);
    assertEquals(1,a.getOptions().size());
    OptionDescriptor i = a.getOption("-l");
    assertEquals(Arrays.asList("l"),i.getNames());
    assertTrue(i.getAnnotation() instanceof Level);
  }
}
