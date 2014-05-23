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
import org.crsh.shell.ScreenContext;

import java.io.IOException;

/**
 * @author Julien Viet
 */
public class ScreenBufferTestCase extends AbstractTestCase {

  static class TestBuffer extends ChunkBuffer implements ScreenContext {

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
    ScreenBuffer buffer = new ScreenBuffer(tmp);
    for (String a : test) {
      buffer.write(Text.create(a));
      buffer.paint();
      assertTrue(buffer.isPainting() || buffer.isPainted());
    }
    assertEquals(actual, tmp.toString());
  }

  public void testPreviousRow() throws Exception {
    TestBuffer tmp = new TestBuffer(2, 2);
    ScreenBuffer buffer = new ScreenBuffer(tmp);
    assertFalse(buffer.previousRow());
    buffer.write(Text.create("abcdef"));
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
    ScreenBuffer buffer = new ScreenBuffer(tmp);
    buffer.write(Text.create("a\nb"));
    assertTrue(buffer.isRefresh());
    assertTrue(buffer.paint().isPainting());
    assertFalse(buffer.nextRow());
    buffer.write(Text.create("\n"));
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
    ScreenBuffer buffer = new ScreenBuffer(tmp);
    buffer.write(Text.create("a\nb\nc\nd\ne\nf"));
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
    ScreenBuffer buffer = new ScreenBuffer(tmp);
    assertTrue(buffer.isRefresh());
    assertTrue(buffer.paint().isPainting());
    assertFalse(buffer.nextRow());
    assertTrue(buffer.isPainting());
  }

  public void testNextRow1() throws Exception {
    TestBuffer tmp = new TestBuffer(2, 2);
    ScreenBuffer buffer = new ScreenBuffer(tmp);
    buffer.write(Text.create("abcd"));
    assertTrue(buffer.isRefresh());
    assertTrue(buffer.paint().isPainted());
    assertFalse(buffer.nextRow());
    assertTrue(buffer.isPainted());
  }

  public void testNextRow2() throws Exception {
    TestBuffer tmp = new TestBuffer(2, 2);
    ScreenBuffer buffer = new ScreenBuffer(tmp);
    buffer.write(Text.create("abcde"));
    assertTrue(buffer.isRefresh());
    assertTrue(buffer.paint().isPainted());
    assertTrue(buffer.nextRow());
    assertTrue(buffer.isRefresh());
    assertTrue(buffer.paint().isPainting());
    assertFalse(buffer.nextRow());
    assertTrue(buffer.isPainting());
    buffer.write(Text.create("fg"));
    assertTrue(buffer.isPainting());
    assertTrue(buffer.paint().isPainted());
    assertTrue(buffer.nextRow());
    assertTrue(buffer.isRefresh());
    assertTrue(buffer.paint().isPainting());
  }

  public void testNextRow3() throws Exception {
    TestBuffer tmp = new TestBuffer(2, 2);
    ScreenBuffer buffer = new ScreenBuffer(tmp);
    buffer.write(Text.create("a\nb"));
    assertFalse(buffer.nextRow());
    assertTrue(buffer.isRefresh());
    assertTrue(buffer.paint().isPainting());
    buffer.write(Text.create("\n"));
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
    ScreenBuffer buffer = new ScreenBuffer(tmp);
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
    ScreenBuffer buffer = new ScreenBuffer(tmp);
    buffer.write(Text.create(s1));
    assertTrue(buffer.isRefresh());
    assertTrue(buffer.paint().isPainting());
    buffer.write(Text.create(s2));
    assertTrue(buffer.isPainting());
    assertTrue(buffer.paint().isPainted());
  }
}
