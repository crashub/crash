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

package org.crsh.term.spi;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import net.wimpi.telnetd.io.TerminalIO;
import org.crsh.term.CodeType;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A term io for simulating a term connector.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestTermIO implements TermIO {

  /** . */
  private final BlockingQueue<Integer> inner;

  /** . */
  private final BlockingQueue<String> outter;

  public TestTermIO() throws IOException {
    this.inner = new LinkedBlockingQueue<Integer>();
    this.outter = new LinkedBlockingQueue<String>();
  }

  public int read() throws IOException {
    try {
      return inner.take();
    }
    catch (InterruptedException e) {
      AssertionFailedError afe = new AssertionFailedError();
      afe.initCause(e);
      throw afe;
    }
  }

  public boolean isClosed() {
    throw new UnsupportedOperationException();
  }

  public TestTermIO appendTab() {
    return append(TerminalIO.TABULATOR);
  }

  public TestTermIO appendDel() {
    return append(TerminalIO.DELETE);
  }

  public TestTermIO appendMoveUp() {
    return append(TerminalIO.UP);
  }

  public TestTermIO appendMoveDown() {
    return append(TerminalIO.DOWN);
  }

  public TestTermIO appendMoveRight() {
    return append(TerminalIO.RIGHT);
  }

  public TestTermIO appendMoveLeft() {
    return append(TerminalIO.LEFT);
  }

  public TestTermIO appendBreak() {
    return append(3);
  }

  public TestTermIO append(char c) {
    return append((int)c);
  }

  public TestTermIO append(CharSequence s) {
    for (int i = 0;i < s.length();i++) {
      char c = s.charAt(i);
      append(c);
    }
    return this;
  }

  private TestTermIO append(int c) {
      // Should assert this is true somehow
      inner.add(c);
      return this;
  }

  public CodeType decode(int code) {
    switch (code) {
      case 3:
        return CodeType.BREAK;
      case TerminalIO.DELETE:
        return CodeType.DELETE;
      case TerminalIO.TABULATOR:
        return CodeType.TAB;
      case TerminalIO.UP:
        return CodeType.UP;
      case TerminalIO.DOWN:
        return CodeType.DOWN;
      case TerminalIO.LEFT:
        return CodeType.LEFT;
      case TerminalIO.RIGHT:
        return CodeType.RIGHT;
      default:
        return CodeType.CHAR;
    }
  }

  public void close() {
    
  }

  public TestTermIO assertChar(char c) {
    return assertRead(String.valueOf(c));
  }

  public TestTermIO assertChars(String s) {
    for (int i = 0;i < s.length();i++) {
      char c = s.charAt(i);
      assertChar(c);
    }
    return this;
  }

  public TestTermIO assertDel() {
    return assertRead("del");
  }

  public TestTermIO assertMoveLeft() {
    return assertRead("left");
  }

  public TestTermIO assertMoveRight() {
    return assertRead("right");
  }

  public TestTermIO assertCRLF() {
    return assertRead("crlf");
  }

  private TestTermIO assertRead(String expected) {
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

  public void flush() throws IOException {
    // For now not tested
  }

  public void write(String s) throws IOException {
    for (int i = 0;i < s.length();i++) {
      char c = s.charAt(i);
      write(c);
    }
  }

  public void write(char c) throws IOException {
    System.out.print("[" + c + "]");
    outter.add("[" + c + "]");
  }

  public void writeDel() throws IOException {
    System.out.print("[del]");
    outter.add("[del]");
  }

  public void writeCRLF() throws IOException {
    System.out.print("[crlf]");
    outter.add("[crlf]");
  }

  public boolean moveRight() throws IOException {
    System.out.print("[right]");
    outter.add("[right]");
    return true;
  }

  public boolean moveLeft() throws IOException {
    System.out.print("[left]");
    outter.add("[left]");
    return true;
  }
}
