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

package org.crsh.cli.impl.type;

import org.crsh.cli.type.ValueType;
import org.crsh.cli.type.ValueTypeFactory;
import junit.framework.TestCase;

import javax.management.ObjectName;
import java.io.File;
import java.util.Properties;

public class SimpleValueTypeTestCase extends TestCase {

  /** . */
  private ValueTypeFactory factory = new ValueTypeFactory(SimpleValueTypeTestCase.class.getClassLoader());

  public void testString() throws Exception {
    ValueType<String> stringVT = factory.get(String.class);
    assertEquals(String.class, stringVT.getType());
    String s = stringVT.parse("abc");
    assertEquals("abc", s);
  }

  public void testInteger() throws Exception {
    ValueType<Integer> stringVT = factory.get(Integer.class);
    assertEquals(Integer.class, stringVT.getType());
    int i = stringVT.parse("123");
    assertEquals(123, i);
  }

  public void testBoolean() throws Exception {
    ValueType<Boolean> booleanVT = factory.get(Boolean.class);
    assertEquals(Boolean.class, booleanVT.getType());
    boolean b = booleanVT.parse("false");
    assertEquals(false, b);
  }

  private static enum Color  {
    RED, BLUE
  }

  public void testEnum() throws Exception {
    ValueType<Enum> stringVT = factory.<Enum, Color>get(Color.class);
    assertEquals(Enum.class, stringVT.getType());
    Color red = stringVT.parse(Color.class, "RED");
    assertEquals(Color.RED, red);
  }

  public void testProperties() throws Exception {
    ValueType<Properties> propertiesVT = factory.get(Properties.class);
    Properties props = propertiesVT.parse("org.apache.jackrabbit.repository.conf=repository" +
        "-in-memory.xml;org.apache.jackrabbit.repository.home=/home/ehugonnet/tmp/crash/jcr/target" +
        "/test-classes/conf/transient");
    assertNotNull(props);
    assertEquals(2, props.size());
    assertEquals("repository-in-memory.xml", props.get("org.apache.jackrabbit.repository.conf"));
    assertEquals("/home/ehugonnet/tmp/crash/jcr/target/test-classes/conf/transient", props.get("org.apache.jackrabbit.repository.home"));
  }

  public void testFile() throws Exception {
    ValueType<File> fileVT = factory.get(File.class);
    File tmp = File.createTempFile("foo", ".bar");
    tmp.deleteOnExit();
    File f = fileVT.parse(tmp.getAbsolutePath());
    assertNotNull(f);
    assertEquals(f, tmp);
  }

  public void testObjectName() throws Exception {
    ValueType<ObjectName> propertiesVT = factory.get(ObjectName.class);
    ObjectName name = propertiesVT.parse(ObjectName.class, "foo:bar=juu");
    assertEquals(new ObjectName("foo", "bar", "juu"), name);
  }
}
