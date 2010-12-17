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
import org.crsh.command.Argument;
import org.crsh.command.Description;
import org.crsh.command.Option;
import org.crsh.util.Utils;

import java.util.Arrays;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ParameterTypeTestCase extends TestCase {

  public void testOptionIntType() throws IntrospectionException {
    class A {
      @Option(names = "-o")
      int o;
    }
    CommandInfo<A> c = CommandInfo.create(A.class);
    OptionInfo i = c.getOption("-o");
    ParameterType t = i.getType();
    assertEquals(Multiplicity.SINGLE, t.getMultiplicity());
    assertEquals(ValueType.INTEGER, t.getValueType());
  }

  public void testOptionIntWrapperType() throws IntrospectionException {
    class A {
      @Option(names = "-o")
      Integer o;
    }
    CommandInfo<A> c = CommandInfo.create(A.class);
    OptionInfo i = c.getOption("-o");
    ParameterType t = i.getType();
    assertEquals(Multiplicity.SINGLE, t.getMultiplicity());
    assertEquals(ValueType.INTEGER, t.getValueType());
  }

  public void testOptionStringType() throws IntrospectionException {
    class A {
      @Option(names = "-o")
      String o;
    }
    CommandInfo<A> c = CommandInfo.create(A.class);
    OptionInfo i = c.getOption("-o");
    ParameterType t = i.getType();
    assertEquals(Multiplicity.SINGLE, t.getMultiplicity());
    assertEquals(ValueType.STRING, t.getValueType());
  }

  public void testOptionBooleanType() throws IntrospectionException {
    class A {
      @Option(names = "-o")
      boolean o;
    }
    CommandInfo<A> c = CommandInfo.create(A.class);
    OptionInfo i = c.getOption("-o");
    ParameterType t = i.getType();
    assertEquals(Multiplicity.SINGLE, t.getMultiplicity());
    assertEquals(ValueType.BOOLEAN, t.getValueType());
  }

  public void testOptionBooleanWrapperType() throws IntrospectionException {
    class A {
      @Option(names = "-o")
      Boolean o;
    }
    CommandInfo<A> c = CommandInfo.create(A.class);
    OptionInfo i = c.getOption("-o");
    ParameterType t = i.getType();
    assertEquals(Multiplicity.SINGLE, t.getMultiplicity());
    assertEquals(ValueType.BOOLEAN, t.getValueType());
  }
}
