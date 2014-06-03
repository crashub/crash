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
package org.crsh.console;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import org.crsh.text.Style;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Julien Viet
 */
public class TestDriver implements ConsoleDriver {

  /** . */
  private final BlockingQueue<String> outter;

  /** . */
  private int width;

  /** . */
  private int height;

  /** . */
  private Map<String, String> properties;

  public TestDriver() {
    this.outter = new LinkedBlockingQueue<String>();
    this.width = 32;
    this.height = 40;
    this.properties = new HashMap<String, String>();
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public void setProperty(String name, String value) {
    if (value != null) {
      properties.put(name, value);
    } else {
      properties.remove(name);
    }
  }

  public String getProperty(String name) {
    return properties.get(name);
  }

  public boolean takeAlternateBuffer() {
    return false;
  }

  public boolean releaseAlternateBuffer() {
    return false;
  }

  public void setWidth(int width) {
    if (width < 1) {
      throw new IllegalArgumentException("No negative width accepted");
    }
    this.width = width;
  }

  public TestDriver assertChar(char c) {
    return assertRead(String.valueOf(c));
  }

  public TestDriver assertChars(String s) {
    for (int i = 0;i < s.length();i++) {
      char c = s.charAt(i);
      assertChar(c);
    }
    return this;
  }

  public TestDriver assertDel() {
    return assertRead("del");
  }

  public TestDriver assertDel(int times) {
    while (times-- > 0) {
      assertDel();
    }
    return this;
  }

  public TestDriver assertMoveLeft() {
    return assertRead("left");
  }

  public TestDriver assertMoveLeft(int times) {
    while (times-- > 0) {
      assertMoveLeft();
    }
    return this;
  }

  public TestDriver assertMoveRight() {
    return assertRead("right");
  }

  public TestDriver assertMoveRight(int times) {
    while (times-- > 0) {
      assertMoveRight();
    }
    return this;
  }

  public TestDriver assertCRLF() {
    return assertRead("crlf");
  }

  public TestDriver assertCLS() {
    return assertRead("cls");
  }

  public TestDriver assertFlush() {
    return assertRead("flush");
  }

  public TestDriver clear() {
    outter.clear();
    return this;
  }

  private TestDriver assertRead(String expected) {
    if (expected.length() == 0) {
      Assert.fail();
    }
    try {
      StringBuilder sb = new StringBuilder();
      sb.append("[");
      for (int i = 0;i < expected.length();i++) {
        char c = expected.charAt(i);
        sb.append(c);
      }
      sb.append("]");
      String s = outter.take();
      Assert.assertEquals(sb.toString(), s);
    }
    catch (InterruptedException e) {
      AssertionFailedError afe = new AssertionFailedError();
      afe.initCause(e);
      throw afe;
    }
    return this;
  }


  public TestDriver assertEmpty() {
    Assert.assertEquals(Collections.<String>emptyList(), new ArrayList<String>(outter));
    return this;
  }

  public void write(CharSequence s) throws IOException {
    write(s, 0, s.length());
  }

  @Override
  public void write(CharSequence s, int start, int end) throws IOException {
    while (start < end) {
      char c = s.charAt(start++);
      write(c);
    }
  }

  @Override
  public void write(char c) throws IOException {
    outter.add("[" + (char)c + "]");
  }

  public void flush() throws IOException {
    outter.add("[flush]");
  }

  public void close() {
    throw new UnsupportedOperationException();
  }

  public void write(Style d) throws IOException {
    throw new UnsupportedOperationException();
  }

  public void cls() throws IOException {
    outter.add("[cls]");
  }

  public void writeDel() throws IOException {
    outter.add("[del]");
  }

  public void writeCRLF() throws IOException {
    outter.add("[crlf]");
  }

  public boolean moveRight(char c) throws IOException {
    outter.add("[right]");
    return true;
  }

  public boolean moveLeft() throws IOException {
    outter.add("[left]");
    return true;
  }
}
