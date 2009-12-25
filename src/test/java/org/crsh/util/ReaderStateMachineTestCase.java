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

import java.io.IOException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ReaderStateMachineTestCase extends TestCase {

  public void testNoCR() throws IOException {
    ReaderStateMachine sm = new ReaderStateMachine(127);
    sm.append("a");
    assertFalse(sm.hasNext());
    assertEquals(1, sm.getSize());
  }

  public void testReadLine() throws IOException {
    String[] tests = {"a\n","a\r","a\n\r","a\r\n"};
    for (String test : tests) {
      ReaderStateMachine sm = new ReaderStateMachine(127);
      sm.append(test);
      assertTrue(sm.hasNext());
      assertEquals("a", sm.next());
      assertFalse(sm.hasNext());
      assertEquals(0, sm.getSize());
    }
  }

  public void testErase() throws IOException {
    ReaderStateMachine sm = new ReaderStateMachine(127);
    sm.append("a\u007Fb\n");
    assertTrue(sm.hasNext());
    assertEquals("b", sm.next());
  }


/*
  public void testFoo() {
    ReaderStateMachine sm = new ReaderStateMachine(127);
    sm.append("a\u007Fb");
//    assertEquals("b", sm.flush());
  }
*/



/*
  public void testBar1() {
    ReaderStateMachine sm = new ReaderStateMachine(127);
    sm.append("ab");
    char[] chars = new char[2];
    assertEquals(1, sm.flush(chars, 0, 1));
    assertEquals('a', chars[0]);
    assertEquals(0, chars[1]);
    assertEquals(1, sm.getSize());
    assertEquals(1, sm.flush(chars, 0, 1));
    assertEquals('b', chars[0]);
    assertEquals(0, chars[1]);
    assertEquals(0, sm.getSize());
  }

  public void testBar2() {
    ReaderStateMachine sm = new ReaderStateMachine(127);
    sm.append("ab");
    char[] chars = new char[2];
    assertEquals(1, sm.flush(chars, 0, 1));
    assertEquals('a', chars[0]);
    assertEquals(0, chars[1]);
    assertEquals(1, sm.getSize());
    assertEquals(1, sm.flush(chars, 0, 2));
    assertEquals('b', chars[0]);
    assertEquals(0, chars[1]);
    assertEquals(0, sm.getSize());
  }

  public void testBar3() {
    ReaderStateMachine sm = new ReaderStateMachine(127);
    sm.append("ab");
    char[] chars = new char[2];
    assertEquals(2, sm.flush(chars, 0, 2));
    assertEquals('a', chars[0]);
    assertEquals('b', chars[1]);
    assertEquals(0, sm.getSize());
    assertEquals(0, sm.flush(chars, 0, 2));
    assertEquals('a', chars[0]);
    assertEquals('b', chars[1]);
    assertEquals(0, sm.getSize());
  }

  public void testBar4() {
    ReaderStateMachine sm = new ReaderStateMachine(127);
    sm.append("ab");
    char[] chars = new char[3];
    assertEquals(2, sm.flush(chars, 0, 3));
    assertEquals('a', chars[0]);
    assertEquals('b', chars[1]);
    assertEquals(0, chars[2]);
    assertEquals(0, sm.getSize());
    assertEquals(0, sm.flush(chars, 0, 1));
    assertEquals('a', chars[0]);
    assertEquals('b', chars[1]);
    assertEquals(0, chars[2]);
    assertEquals(0, sm.getSize());
  }
*/

}
