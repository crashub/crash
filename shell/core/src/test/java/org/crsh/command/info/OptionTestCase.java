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

package org.crsh.command.info;

import junit.framework.TestCase;
import org.crsh.command.Option;

import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class OptionTestCase extends TestCase {

  public void testIllegalParameter() throws IntrospectionException {

    class A {
      @Option(opt = 'o', arity = 2)
      int o;
    }
    assertIllegalParameter(A.class);

    class C {
      @Option(opt = 'o')
      List<Boolean> o;
    }
    assertIllegalParameter(C.class);
  }

  public void testIllegalTypes() throws IntrospectionException {

    class A {
      @Option(opt = 'o')
      Exception o;
    }
    assertIllegalValueType(A.class);

    class B {
      @Option(opt = 'o')
      List<Exception> o;
    }
    assertIllegalValueType(B.class);

    class C {
      @Option(opt = 'o')
      double o;
    }
    assertIllegalValueType(C.class);

    class D {
      @Option(opt = 'o')
      Double o;
    }
    assertIllegalValueType(D.class);

    class E {
      @Option(opt = 'o')
      List<Double> o;
    }
    assertIllegalValueType(E.class);

  }

  public void testOptionIntType() throws IntrospectionException {
    class A {
      @Option(opt = 'o')
      int o;
    }
    CommandInfo<A> c = CommandInfo.create(A.class);
    OptionInfo i = c.getOption("-o");
    ValueType t = i.getType();
    assertEquals(Multiplicity.SINGLE, t.getMultiplicity());
    assertEquals(SimpleValueType.INTEGER, t.getValueType());
  }

  public void testOptionIntWrapperType() throws IntrospectionException {
    class A {
      @Option(opt = 'o')
      Integer o;
    }
    CommandInfo<A> c = CommandInfo.create(A.class);
    OptionInfo i = c.getOption("-o");
    ValueType t = i.getType();
    assertEquals(Multiplicity.SINGLE, t.getMultiplicity());
    assertEquals(SimpleValueType.INTEGER, t.getValueType());
  }

  public void testOptionIntListType() throws IntrospectionException {
    class A {
      @Option(opt = 'o')
      List<Integer> o;
    }
    CommandInfo<A> c = CommandInfo.create(A.class);
    OptionInfo i = c.getOption("-o");
    ValueType t = i.getType();
    assertEquals(Multiplicity.LIST, t.getMultiplicity());
    assertEquals(SimpleValueType.INTEGER, t.getValueType());
  }

  public void testOptionStringType() throws IntrospectionException {
    class A {
      @Option(opt = 'o')
      String o;
    }
    CommandInfo<A> c = CommandInfo.create(A.class);
    OptionInfo i = c.getOption("-o");
    ValueType t = i.getType();
    assertEquals(Multiplicity.SINGLE, t.getMultiplicity());
    assertEquals(SimpleValueType.STRING, t.getValueType());
  }

  public void testOptionStringListType() throws IntrospectionException {
    class A {
      @Option(opt = 'o')
      List<String> o;
    }
    CommandInfo<A> c = CommandInfo.create(A.class);
    OptionInfo i = c.getOption("-o");
    ValueType t = i.getType();
    assertEquals(Multiplicity.LIST, t.getMultiplicity());
    assertEquals(SimpleValueType.STRING, t.getValueType());
  }

  public void testOptionBooleanType() throws IntrospectionException {
    class A {
      @Option(opt = 'o')
      boolean o;
    }
    CommandInfo<A> c = CommandInfo.create(A.class);
    OptionInfo i = c.getOption("-o");
    ValueType t = i.getType();
    assertEquals(Multiplicity.SINGLE, t.getMultiplicity());
    assertEquals(SimpleValueType.BOOLEAN, t.getValueType());
  }

  public void testOptionBooleanWrapperType() throws IntrospectionException {
    class A {
      @Option(opt = 'o')
      Boolean o;
    }
    CommandInfo<A> c = CommandInfo.create(A.class);
    OptionInfo i = c.getOption("-o");
    ValueType t = i.getType();
    assertEquals(Multiplicity.SINGLE, t.getMultiplicity());
    assertEquals(SimpleValueType.BOOLEAN, t.getValueType());
  }

  private void assertIllegalValueType(Class<?> type) throws IntrospectionException {
    try {
      CommandInfo.create(type);
      fail();
    }
    catch (IllegalValueTypeException e) {
    }
  }

  private void assertIllegalParameter(Class<?> type) throws IntrospectionException {
    try {
      CommandInfo.create(type);
      fail();
    }
    catch (IllegalParameterException e) {
    }
  }
}
