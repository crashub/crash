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
import org.crsh.cli.impl.matcher.Custom;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;

public class MissingValueTypeTestCase extends TestCase {

  public void testFoo() {
    final URL url = MissingValueTypeTestCase.class.getResource("InvalidValueType");
    assertNotNull(url);
    ClassLoader cl = new ClassLoader(MissingValueTypeTestCase.class.getClassLoader()) {
      @Override
      public Enumeration<URL> getResources(String name) throws IOException {
        if (name.equals("META-INF/services/" + ValueType.class.getName())) {
          return Collections.enumeration(Collections.singleton(url));
        } else {
          return super.getResources(name);
        }
      }
    };
    ValueTypeFactory factory = new ValueTypeFactory(cl);
    ValueType<Custom> custom = factory.get(Custom.class);
    assertNotNull(custom);
  }

}
