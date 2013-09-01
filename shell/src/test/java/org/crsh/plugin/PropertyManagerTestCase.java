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
package org.crsh.plugin;

import org.crsh.AbstractTestCase;
import org.crsh.auth.SimpleAuthenticationPlugin;

public class PropertyManagerTestCase extends AbstractTestCase {

  class Foo {}
  PropertyDescriptor<Foo> FOO = new PropertyDescriptor<Foo>(Foo.class, "foo", null, "the foo") {
    @Override
    protected Foo doParse(String s) throws Exception {
      throw new UnsupportedOperationException();
    }
  };
  Foo foo = new Foo();

  public void testResolveProperty() {
    PropertyManager mgr = new PropertyManager();
    mgr.setProperty(FOO, foo);
    assertEquals(foo, mgr.resolvePropertyValue(FOO));
    Property<? extends Foo> property = mgr.resolveProperty(FOO);
    assertEquals(foo, property.getValue());
    Property<? extends Foo> asInteger = mgr.resolveProperty("foo", Foo.class);
    assertEquals(foo, asInteger.getValue());
    Property<?> asObject = mgr.resolveProperty("foo", Object.class);
    assertEquals(foo, asObject.getValue());
  }

  public void testGetProperty() {
    PropertyManager mgr = new PropertyManager();
    mgr.setProperty(FOO, foo);
    Property<Foo> property = mgr.getProperty(FOO);
    assertEquals(foo, property.getValue());
    Property<Foo> asInteger = mgr.getProperty("foo", Foo.class);
    assertEquals(foo, asInteger.getValue());
    Property<Object> asObject = mgr.getProperty("foo", Object.class);
    assertNull(asObject);
  }

  public void testDisplayValue() {
    PropertyManager mgr = new PropertyManager();
    mgr.setProperty(PropertyDescriptor.VFS_REFRESH_PERIOD, 4);
    Property<Integer> property = mgr.getProperty(PropertyDescriptor.VFS_REFRESH_PERIOD);
    assertEquals("4", property.getDisplayValue());
    mgr.setProperty(SimpleAuthenticationPlugin.SIMPLE_PASSWORD, "the_password");
    Property<String> secretProperty = mgr.getProperty(SimpleAuthenticationPlugin.SIMPLE_PASSWORD);
    assertEquals(PropertyDescriptor.SECRET_DISPLAY_VALUE, secretProperty.getDisplayValue());
    assertEquals("the_password", secretProperty.getValue());
  }
}
