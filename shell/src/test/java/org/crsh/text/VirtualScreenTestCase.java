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
package org.crsh.text;

import org.crsh.AbstractTestCase;

import java.io.IOException;

/**
 * @author Julien Viet
 */
public class VirtualScreenTestCase extends AbstractTestCase {

  static class TestBuffer extends ScreenBuffer implements ScreenContext {

    int width, height;

    TestBuffer(int width, int height) {
      this.width = width;
      this.height = height;
    }

    public int getWidth() {
      return width;
    }

    public int getHeight() {
      return height;
    }
  }

  public void testWrite() throws Exception {
    assertBuffer(2, 2, "abcd", "abcde");
    assertBuffer(2, 2, "abcd", "a", "b", "c", "d", "e");
    assertBuffer(2, 2, "a\nbc", "a\nb", "c", "d");
    assertBuffer(2, 2, "a\nb", "a\n", "b\n", "c");
    assertBuffer(2, 2, "a\nb", "a", "\n", "b", "\n", "c");
    assertBuffer(2, 2, "\n","\n", "\n", "a");
    assertBuffer(2, 2, "", "");
  }

  private void assertBuffer(int width, int height, String actual, String... test) throws IOException {
    TestBuffer tmp = new TestBuffer(width, height);
    VirtualScreen buffer = new VirtualScreen(tmp);
    for (String a : test) {
      buffer.append(a);
      buffer.paint();
      assertTrue(buffer.isPainting() || buffer.isPainted());
    }
    assertEquals(actual, tmp.toString());
  }

  public void testPreviousRow() throws Exception {
    TestBuffer tmp = new TestBuffer(2, 2);
    VirtualScreen buffer = new VirtualScreen(tmp);
    assertFalse(buffer.previousRow());
    buffer.append("abcdef");
    assertTrue(buffer.isRefresh());
    assertTrue(buffer.paint().isPainted());
    assertFalse(buffer.previousRow());
    assertTrue(buffer.nextRow());
    tmp.clear();
    assertTrue(buffer.previousRow());
    assertTrue(buffer.isRefresh());
    assertTrue(buffer.paint().isPainted());
    assertEquals("abcd", tmp.toString());
    assertFalse(buffer.previousRow());
  }

  public void testPreviousRow2() throws Exception {
    TestBuffer tmp = new TestBuffer(2, 2);
    VirtualScreen buffer = new VirtualScreen(tmp);
    buffer.append("a\nb");
    assertTrue(buffer.isRefresh());
    assertTrue(buffer.paint().isPainting());
    assertFalse(buffer.nextRow());
    buffer.append("\n");
    assertTrue(buffer.paint().isPainted());
    assertTrue(buffer.nextRow());
    tmp.clear();
    assertTrue(buffer.previousRow());
    assertTrue(buffer.isRefresh());
    assertTrue(buffer.paint().isPainted());
    assertEquals("a\nb", tmp.toString());
  }

  public void testPreviewRow3() throws Exception {
    TestBuffer tmp = new TestBuffer(2, 2);
    VirtualScreen buffer = new VirtualScreen(tmp);
    buffer.append("a\nb\nc\nd\ne\nf");
    assertTrue(buffer.isRefresh());
    assertTrue(buffer.paint().isPainted());
    assertTrue(buffer.nextRow());
    assertTrue(buffer.isRefresh());
    assertTrue(buffer.paint().isPainted());
    assertTrue(buffer.nextRow());
    assertTrue(buffer.isRefresh());
    assertTrue(buffer.paint().isPainted());
    tmp.clear();
    assertTrue(buffer.previousRow());
    assertTrue(buffer.isRefresh());
    assertTrue(buffer.paint().isPainted());
    assertEquals("b\nc", tmp.toString());
  }

  public void testNextRow0() throws Exception {
    TestBuffer tmp = new TestBuffer(2, 2);
    VirtualScreen buffer = new VirtualScreen(tmp);
    assertTrue(buffer.isRefresh());
    assertTrue(buffer.paint().isPainting());
    assertFalse(buffer.nextRow());
    assertTrue(buffer.isPainting());
  }

  public void testNextRow1() throws Exception {
    TestBuffer tmp = new TestBuffer(2, 2);
    VirtualScreen buffer = new VirtualScreen(tmp);
    buffer.append("abcd");
    assertTrue(buffer.isRefresh());
    assertTrue(buffer.paint().isPainted());
    assertFalse(buffer.nextRow());
    assertTrue(buffer.isPainted());
  }

  public void testNextRow2() throws Exception {
    TestBuffer tmp = new TestBuffer(2, 2);
    VirtualScreen buffer = new VirtualScreen(tmp);
    buffer.append("abcde");
    assertTrue(buffer.isRefresh());
    assertTrue(buffer.paint().isPainted());
    assertTrue(buffer.nextRow());
    assertTrue(buffer.isRefresh());
    assertTrue(buffer.paint().isPainting());
    assertFalse(buffer.nextRow());
    assertTrue(buffer.isPainting());
    buffer.append("fg");
    assertTrue(buffer.isPainting());
    assertTrue(buffer.paint().isPainted());
    assertTrue(buffer.nextRow());
    assertTrue(buffer.isRefresh());
    assertTrue(buffer.paint().isPainting());
  }

  public void testNextRow3() throws Exception {
    TestBuffer tmp = new TestBuffer(2, 2);
    VirtualScreen buffer = new VirtualScreen(tmp);
    buffer.append("a\nb");
    assertFalse(buffer.nextRow());
    assertTrue(buffer.isRefresh());
    assertTrue(buffer.paint().isPainting());
    buffer.append("\n");
    assertTrue(buffer.isPainting());
    assertTrue(buffer.paint().isPainted());
    tmp.clear();
    assertTrue(buffer.nextRow());
    assertTrue(buffer.isRefresh());
    assertTrue(buffer.paint().isPainting());
    assertEquals("b\n", tmp.toString());
  }

  public void testRefresh() throws Exception {
    TestBuffer tmp = new TestBuffer(2, 2);
    VirtualScreen buffer = new VirtualScreen(tmp);
    tmp.width = 3;
    assertFalse(buffer.update());
  }

  public void testPainted() throws Exception {
    assertPainted("abc", "d");
    assertPainted("abc", "\n");
    assertPainted("\n", "ab");
    assertPainted("\n", "\n");
  }

  public void assertPainted(String s1, String s2) throws Exception {
    TestBuffer tmp = new TestBuffer(2, 2);
    VirtualScreen buffer = new VirtualScreen(tmp);
    buffer.append(s1);
    assertTrue(buffer.isRefresh());
    assertTrue(buffer.paint().isPainting());
    buffer.append(s2);
    assertTrue(buffer.isPainting());
    assertTrue(buffer.paint().isPainted());
  }
}
