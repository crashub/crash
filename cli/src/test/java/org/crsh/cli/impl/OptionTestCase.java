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

import org.crsh.cli.descriptor.CommandDescriptor;
import org.crsh.cli.descriptor.OptionDescriptor;
import junit.framework.TestCase;
import org.crsh.cli.Option;
import org.crsh.cli.impl.descriptor.IllegalParameterException;
import org.crsh.cli.impl.descriptor.IllegalValueTypeException;
import org.crsh.cli.impl.descriptor.IntrospectionException;
import org.crsh.cli.impl.lang.Instance;
import org.crsh.cli.type.ValueType;
import org.crsh.cli.impl.lang.CommandFactory;

import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class OptionTestCase extends TestCase {

  public void testIllegalParameter() throws IntrospectionException {
    class A {
      @Option(names = "o")
      List<Boolean> o;
    }
    assertIllegalParameter(A.class);
  }

  public void testIllegalTypes() throws IntrospectionException {

    class A {
      @Option(names = "o")
      Exception o;
    }
    assertIllegalValueType(A.class);

    class B {
      @Option(names = "o")
      List<Exception> o;
    }
    assertIllegalValueType(B.class);

    class C {
      @Option(names = "o")
      double o;
    }
    assertIllegalValueType(C.class);

    class D {
      @Option(names = "o")
      Double o;
    }
    assertIllegalValueType(D.class);

    class E {
      @Option(names = "o")
      List<Double> o;
    }
    assertIllegalValueType(E.class);

  }

  public void testOptionIntType() throws IntrospectionException {
    class A {
      @Option(names = "o")
      int o;
    }
    CommandDescriptor<Instance<A>> c = CommandFactory.DEFAULT.create(A.class);
    OptionDescriptor i = c.getOption("-o");
    assertEquals(Multiplicity.SINGLE, i.getMultiplicity());
    assertEquals(false, i.isRequired());
    assertEquals(ValueType.INTEGER, i.getType());
  }

  public void testOptionIntWrapperType() throws IntrospectionException {
    class A {
      @Option(names = "o")
      Integer o;
    }
    CommandDescriptor<Instance<A>> c = CommandFactory.DEFAULT.create(A.class);
    OptionDescriptor i = c.getOption("-o");
    assertEquals(Multiplicity.SINGLE, i.getMultiplicity());
    assertEquals(false, i.isRequired());
    assertEquals(ValueType.INTEGER, i.getType());
  }

  public void testOptionIntListType() throws IntrospectionException {
    class A {
      @Option(names = "o")
      List<Integer> o;
    }
    CommandDescriptor<Instance<A>> c = CommandFactory.DEFAULT.create(A.class);
    OptionDescriptor i = c.getOption("-o");
    assertEquals(Multiplicity.MULTI, i.getMultiplicity());
    assertEquals(ValueType.INTEGER, i.getType());
  }

  public void testOptionStringType() throws IntrospectionException {
    class A {
      @Option(names = "o")
      String o;
    }
    CommandDescriptor<Instance<A>> c = CommandFactory.DEFAULT.create(A.class);
    OptionDescriptor i = c.getOption("-o");
    assertEquals(Multiplicity.SINGLE, i.getMultiplicity());
    assertEquals(false, i.isRequired());
    assertEquals(ValueType.STRING, i.getType());
  }

  public void testOptionStringListType() throws IntrospectionException {
    class A {
      @Option(names = "o")
      List<String> o;
    }
    CommandDescriptor<Instance<A>> c = CommandFactory.DEFAULT.create(A.class);
    OptionDescriptor i = c.getOption("-o");
    assertEquals(Multiplicity.MULTI, i.getMultiplicity());
    assertEquals(ValueType.STRING, i.getType());
  }

  public void testOptionBooleanType() throws IntrospectionException {
    class A {
      @Option(names = "o")
      boolean o;
    }
    CommandDescriptor<Instance<A>> c = CommandFactory.DEFAULT.create(A.class);
    OptionDescriptor i = c.getOption("-o");
    assertEquals(Multiplicity.SINGLE, i.getMultiplicity());
    assertEquals(false, i.isRequired());
    assertEquals(ValueType.BOOLEAN, i.getType());
  }

  public void testOptionBooleanWrapperType() throws IntrospectionException {
    class A {
      @Option(names = "o")
      Boolean o;
    }
    CommandDescriptor<Instance<A>> c = CommandFactory.DEFAULT.create(A.class);
    OptionDescriptor i = c.getOption("-o");
    assertEquals(Multiplicity.SINGLE, i.getMultiplicity());
    assertEquals(false, i.isRequired());
    assertEquals(ValueType.BOOLEAN, i.getType());
  }

  public void testOptionEnumType() throws IntrospectionException {
    class A {
      @Option(names = "o")
      RetentionPolicy o;
    }
    CommandDescriptor<Instance<A>> c = CommandFactory.DEFAULT.create(A.class);
    OptionDescriptor i = c.getOption("-o");
    assertEquals(Multiplicity.SINGLE, i.getMultiplicity());
    assertEquals(false, i.isRequired());
    assertEquals(ValueType.ENUM, i.getType());
  }

  public void testSingleLetterName() throws Exception {
    class A {
      @Option(names = "o")
      RetentionPolicy o;
    }
    OptionDescriptor i = CommandFactory.DEFAULT.create(A.class).getOption("-o");
    assertEquals(Arrays.asList("o"), i.getNames());

    //
    class B {
      @Option(names = "-")
      RetentionPolicy o;
    }
    assertIllegalParameter(B.class);

    //
    class C {
      @Option(names = "_")
      RetentionPolicy o;
    }
    assertIllegalParameter(C.class);
  }

  public void testTwoLettersName() throws Exception {
    class A {
      @Option(names = "op")
      RetentionPolicy o;
    }
    OptionDescriptor i = CommandFactory.DEFAULT.create(A.class).getOption("--op");
    assertEquals(Arrays.asList("op"), i.getNames());

    //
    class B {
      @Option(names = "-o")
      RetentionPolicy o;
    }
    assertIllegalParameter(B.class);

    //
    class C {
      @Option(names = "o-")
      RetentionPolicy o;
    }
    assertIllegalParameter(C.class);
  }

  public void testThreeLettersName() throws Exception {
    class A {
      @Option(names = "opt")
      RetentionPolicy o;
    }
    OptionDescriptor i = CommandFactory.DEFAULT.create(A.class).getOption("--opt");
    assertEquals(Arrays.asList("opt"), i.getNames());

    //
    class B {
      @Option(names = "-oo")
      RetentionPolicy o;
    }
    assertIllegalParameter(B.class);

    //
    class C {
      @Option(names = "oo-")
      RetentionPolicy o;
    }
    assertIllegalParameter(C.class);

    //
    class D {
      @Option(names = "o-o")
      RetentionPolicy o;
    }
    i = CommandFactory.DEFAULT.create(D.class).getOption("--o-o");
    assertEquals(Arrays.asList("o-o"), i.getNames());
  }

  private void assertIllegalValueType(Class<?> type) throws IntrospectionException {
    try {
      CommandFactory.DEFAULT.create(type);
      fail();
    }
    catch (IllegalValueTypeException e) {
    }
  }

  private void assertIllegalParameter(Class<?> type) throws IntrospectionException {
    try {
      CommandFactory.DEFAULT.create(type);
      fail();
    }
    catch (IllegalParameterException e) {
    }
  }
}
