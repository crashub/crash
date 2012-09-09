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

package org.crsh.term.console;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import org.crsh.text.Style;

import java.io.IOException;

public class TestClientOutput extends ViewWriter {

  /** . */
  private final StringBuilder line = new StringBuilder();

  /** The last position. */
  private int lastPosition = 0;

  /** . */
  private int position = 0;

  /** . */
  private final boolean supportsCursorMove;

  public TestClientOutput(boolean supportsCursorMove) {
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

  @Override
  protected void flush() throws IOException {
    // Noop until we test it
  }

  @Override
  protected void write(CharSequence s) throws IOException {
    for (int i = 0;i < s.length();i++) {
      char c = s.charAt(i);
      write(c);
    }
  }

  @Override
  protected void write(char c) throws IOException {
    if (c == '\r' || c == '\n') {
      throw new AssertionFailedError();
    }
    if (position < line.length()) {
      line.setCharAt(position++, c);
    } else
    {
      line.append(c);
      position++;
    }
  }

  @Override
  protected void write(Style style) throws IOException {
  }

  @Override
  protected void writeDel() throws IOException {
    if (position > lastPosition) {
      line.deleteCharAt(--position);
    } else {
      throw new AssertionFailedError();
    }
  }

  @Override
  protected boolean writeMoveRight(char c) {
    if (supportsCursorMove) {
      position++;
      return true;
    } else {
      return false;
    }
  }

  @Override
  protected void writeCRLF() throws IOException {
    line.append("\r\n");
    position += 2;
    lastPosition = position;
  }

  @Override
  protected boolean writeMoveLeft() {
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
