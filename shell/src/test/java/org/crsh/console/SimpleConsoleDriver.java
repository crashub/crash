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

public class SimpleConsoleDriver implements ConsoleDriver {

  /** . */
  private final StringBuilder line = new StringBuilder();

  /** The last position. */
  private int lastPosition = 0;

  /** . */
  private int position = 0;

  /** . */
  private final boolean supportsCursorMove;

  public SimpleConsoleDriver(boolean supportsCursorMove) {
    this.supportsCursorMove = supportsCursorMove;
  }

  public void assertChars(String s) {
    Assert.assertTrue(line.length() >= s.length());
    Assert.assertEquals(s, line.substring(0, s.length()));
    line.delete(0, s.length());
  }

  public void assertEmpty() {
    Assert.assertEquals("Was expecting empty line instead of '" + line + "'", 0, line.length());
  }

  public int read() throws IOException {
    throw new UnsupportedOperationException();
  }

  public int getWidth() {
    throw new UnsupportedOperationException();
  }

  public int getHeight() {
    throw new UnsupportedOperationException();
  }

  public String getProperty(String name) {
    throw new UnsupportedOperationException();
  }

  public boolean takeAlternateBuffer() {
    throw new UnsupportedOperationException();
  }

  public boolean releaseAlternateBuffer() {
    throw new UnsupportedOperationException();
  }

  public void close() throws IOException {
    throw new UnsupportedOperationException();
  }

  public void flush() throws IOException {
    // Noop until we test it
  }

  public void cls() throws IOException {
    line.setLength(0);
    position = 0;
  }

  @Override
  public void write(CharSequence s, int start, int end) throws IOException {
    while (start < end) {
      char c = s.charAt(start++);
      write(c);
    }
  }

  public void write(CharSequence s) throws IOException {
    write(s, 0, s.length());
  }

  @Override
  public void write(char c) throws IOException {
    if (c == '\r' || c == '\n') {
      throw new AssertionFailedError();
    }
    if (position < line.length()) {
      line.setCharAt(position++, (char)c);
    } else
    {
      line.appendCodePoint(c);
      position++;
    }
  }

  public void write(Style style) throws IOException {
  }

  public void writeDel() throws IOException {
    if (position > lastPosition) {
      line.deleteCharAt(--position);
    } else {
      throw new AssertionFailedError();
    }
  }

  public boolean moveRight(char c) {
    if (supportsCursorMove && position < line.length()) {
      line.setCharAt(position++, c);
      return true;
    } else {
      return false;
    }
  }

  public void writeCRLF() throws IOException {
    line.append("\r\n");
    position = line.length();
    lastPosition = position;
  }

  public boolean moveLeft() {
    if (supportsCursorMove) {
      if (position > lastPosition) {
        position--;
      } else {
        throw new AssertionFailedError();
      }
      return true;
    } else {
      return false;
    }
  }
}
