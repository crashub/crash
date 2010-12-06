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
import java.io.PipedReader;
import java.io.PipedWriter;

/**
 * A term io for simulating a term connector.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestTermConnector implements TermIO {

  /** . */
  private final PipedReader innerReader;

  /** . */
  private final PipedWriter innerWriter;

  /** . */
  private final PipedReader outterReader;

  /** . */
  private final PipedWriter outterWriter;

  public TestTermConnector() throws IOException {
    this.outterWriter = new PipedWriter();
    this.outterReader = new PipedReader(outterWriter);
    this.innerWriter = new PipedWriter();
    this.innerReader = new PipedReader(innerWriter);
  }

  public int read() throws IOException {
    return innerReader.read();
  }

  public TestTermConnector appendDel() {
    return append(TerminalIO.DELETE);
  }

  public TestTermConnector appendMoveUp() {
    return append(TerminalIO.UP);
  }

  public TestTermConnector appendMoveDown() {
    return append(TerminalIO.DOWN);
  }

  public TestTermConnector appendMoveRight() {
    return append(TerminalIO.RIGHT);
  }

  public TestTermConnector appendMoveLeft() {
    return append(TerminalIO.LEFT);
  }

  public TestTermConnector appendBreak() {
    return append(3);
  }

  public TestTermConnector append(char c) {
    return append((int)c);
  }

  public TestTermConnector append(CharSequence s) {
    for (int i = 0;i < s.length();i++) {
      char c = s.charAt(i);
      append(c);
    }
    return this;
  }

  private TestTermConnector append(int c) {
    try {
      innerWriter.write(c);
      return this;
    }
    catch (IOException e) {
      AssertionFailedError afe = new AssertionFailedError();
      afe.initCause(e);
      throw afe;
    }
  }

  public CodeType decode(int code) {
    switch (code) {
      case 3:
        return CodeType.BREAK;
      case TerminalIO.DELETE:
        return CodeType.DELETE;
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

  public TestTermConnector assertChar(char c) {
    return assertRead(String.valueOf(c));
  }

  public TestTermConnector assertChars(String s) {
    for (int i = 0;i < s.length();i++) {
      char c = s.charAt(i);
      assertChar(c);
    }
    return this;
  }

  public TestTermConnector assertDel() {
    return assertRead("del");
  }

  public TestTermConnector assertMoveLeft() {
    return assertRead("left");
  }

  public TestTermConnector assertMoveRight() {
    return assertRead("right");
  }

  public TestTermConnector assertCRLF() {
    return assertRead("crlf");
  }

  private TestTermConnector assertRead(String expected) {
    if (expected.length() == 0) {
      Assert.fail();
    }
    try {
      Assert.assertEquals('[', outterReader.read());
      for (int i = 0;i < expected.length();i++) {
        char c = expected.charAt(i);
        Assert.assertEquals(c, outterReader.read());
      }
      Assert.assertEquals(']', outterReader.read());
    }
    catch (IOException e) {
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
    outterWriter.write('[');
    outterWriter.write(c);
    outterWriter.write(']');
  }

  public void writeDel() throws IOException {
    System.out.print("[del]");
    outterWriter.write("[del]");
  }

  public void writeCRLF() throws IOException {
    System.out.print("[crlf]");
    outterWriter.write("[crlf]");
  }

  public boolean moveRight() throws IOException {
    System.out.print("[right]");
    outterWriter.write("[right]");
    return true;
  }

  public boolean moveLeft() throws IOException {
    System.out.print("[left]");
    outterWriter.write("[left]");
    return true;
  }
}
