/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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

import java.util.concurrent.atomic.AtomicReference;

/**
 * Should contain more test later.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class LatchedFutureTestCase extends TestCase {

  public void testBasic() throws Exception {
    LatchedFuture<String> s = new LatchedFuture<String>();
    assertFalse(s.isDone());
    s.set("foo");
    assertTrue(s.isDone());
    assertEquals("foo", s.get());
  }

  public void testListener1() throws Exception {
    LatchedFuture<String> s = new LatchedFuture<String>();
    final AtomicReference<String> ref = new AtomicReference<String>();
    s.addListener(new FutureListener<String>() {
      public void completed(String value) {
        ref.set(value);
      }
    });
    assertNull(ref.get());
    s.set("bar");
    assertEquals("bar", ref.get());
  }

  public void testListener2() throws Exception {
    LatchedFuture<String> s = new LatchedFuture<String>();
    s.set("bar");
    final AtomicReference<String> ref = new AtomicReference<String>();
    s.addListener(new FutureListener<String>() {
      public void completed(String value) {
        ref.set(value);
      }
    });
    assertEquals("bar", ref.get());
  }
}
