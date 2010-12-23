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
    CommandDescriptor<A, ?> c = CommandDescriptor.create(A.class);
    assertEquals("", c.getDescription());
    assertEquals(0, c.getArguments().size());
    assertEquals(0, c.getOptions().size());
  }

  public void testCommandDescription() throws IntrospectionException {
    class A {
    }
    CommandDescriptor<A, ?> c = CommandDescriptor.create(A.class);
    assertEquals("", c.getDescription());
    assertEquals(0, c.getArguments().size());
    assertEquals(0, c.getOptions().size());
  }

  public void testOption() throws IntrospectionException {
    class A {
      @Option(names = "i")
      private int i;
    }
    CommandDescriptor<A, ?> ai = CommandDescriptor.create(A.class);
    assertEquals(1,ai.getOptions().size());
    OptionDescriptor i = ai.getOption("-i");
    assertEquals(Arrays.asList("i"),i.getNames());
  }

  public void testArgument1() throws IntrospectionException {
    class A {
      @Argument()
      private int i;
    }
    CommandDescriptor<A, ?> c = CommandDescriptor.create(A.class);
    assertEquals(1, c.getArguments().size());
    ArgumentDescriptor i = c.getArguments().get(0);
    assertEquals(SimpleValueType.INTEGER, i.getType().getValueType());
    assertEquals(Multiplicity.SINGLE, i.getType().getMultiplicity());
  }

  public void testArgument2() throws IntrospectionException {
    class A {
      @Argument
      private int i;
      @Argument
      private List<Integer> j;
    }
    CommandDescriptor<A, ?> c = CommandDescriptor.create(A.class);
    assertEquals(2, c.getArguments().size());
    ArgumentDescriptor i = c.getArguments().get(0);
    assertEquals(SimpleValueType.INTEGER, i.getType().getValueType());
    assertEquals(Multiplicity.SINGLE, i.getType().getMultiplicity());
    ArgumentDescriptor j = c.getArguments().get(1);
    assertEquals(SimpleValueType.INTEGER, j.getType().getValueType());
    assertEquals(Multiplicity.LIST, j.getType().getMultiplicity());
  }

  public void testArgument3() throws IntrospectionException {
    class A {
      @Argument
      private List<Integer> i;
      @Argument
      private List<Integer> j;
    }
    try {
      CommandDescriptor.create(A.class);
      fail();
    }
    catch (IntrospectionException e) {
    }
  }

  public void testSub() throws IntrospectionException {

    class A {
      @Command
      void b() {
      }
    }

    ClassDescriptor<A> a = CommandDescriptor.create(A.class);
    MethodDescriptor<?> b = a.getMethod("b");
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
      CommandDescriptor.create(A.class);
      fail();
    }
    catch (IntrospectionException e) {
    }
  }
}
