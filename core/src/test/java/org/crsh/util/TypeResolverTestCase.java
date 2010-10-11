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

package org.crsh.util;

import junit.framework.TestCase;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TypeResolverTestCase extends TestCase {

  public void testFoo() {
    Type resolved = TypeResolver.resolve(ThreadLocal.class, ThreadLocal.class, 0);
    assertTrue(resolved instanceof TypeVariable);
    TypeVariable tv = (TypeVariable)resolved;
    assertEquals(ThreadLocal.class, tv.getGenericDeclaration());
  }

  public void testBar() {
    class A extends ThreadLocal<String> {}
    Type resolved = TypeResolver.resolve(A.class, ThreadLocal.class, 0);
    assertEquals(String.class, resolved);
  }

  public void testJuu() {
    class A extends InheritableThreadLocal<String> {}
    Type resolved = TypeResolver.resolve(A.class, ThreadLocal.class, 0);
    assertEquals(String.class, resolved);
  }

  public void testDaa() {
    Type resolved = TypeResolver.resolve(InheritableThreadLocal.class, ThreadLocal.class, 0);
    assertTrue(resolved instanceof TypeVariable);
    TypeVariable tv = (TypeVariable)resolved;
    assertEquals(InheritableThreadLocal.class, tv.getGenericDeclaration());
  }
}
