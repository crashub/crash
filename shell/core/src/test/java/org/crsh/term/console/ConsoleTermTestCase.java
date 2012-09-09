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

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import org.crsh.term.Term;
import org.crsh.term.TermEvent;
import org.crsh.term.console.ConsoleTerm;
import org.crsh.term.spi.TestTermIO;

import java.io.IOException;

public class ConsoleTermTestCase extends TestCase {

  /** . */
  private Term term;

  /** . */
  private TestTermIO io;

  @Override
  protected void setUp() throws Exception {
    this.io = new TestTermIO();
    this.term = new ConsoleTerm(io);
  }

  private TermEvent assertEvent() {
    try {
      return term.read();
    }
    catch (IOException e) {
      AssertionFailedError afe = new AssertionFailedError();
      afe.initCause(e);
      throw afe;
    }
  }

  private void assertLine(String expected) {
    TermEvent event = assertEvent();
    assertTrue(event instanceof TermEvent.ReadLine);
    TermEvent.ReadLine readLine = (TermEvent.ReadLine)event;
    assertEquals(expected, readLine.getLine());
  }

  private void assertBreak() {
    TermEvent event = assertEvent();
    assertTrue(event instanceof TermEvent.Break);
  }

  private void assertComplete(String expected) {
    TermEvent event = assertEvent();
    assertTrue(event instanceof TermEvent.Complete);
    TermEvent.Complete complete = (TermEvent.Complete)event;
    assertEquals(expected, complete.getLine());
  }

  public void testLine() throws Exception {
    io.append("abc\r\n");
    assertLine("abc");
  }

  public void testDel() throws Exception {
    io.append("abc");
    io.appendDel();
    io.append("\r\n");
    assertLine("ab");
  }

  public void testBreak() throws Exception {
    io.append("abc");
    io.appendBreak();
    assertBreak();
  }

  public void testInsert() throws Exception {
    io.append("ab");
    io.appendMoveLeft();
    io.append("c\r\n");
    assertLine("acb");
  }

  public void testIdempotentMoveRight() throws Exception {
    io.append("a");
    io.appendMoveRight();
    io.append("\r\n");
    assertLine("a");
  }

  public void testIdempotentMoveLeft() throws Exception {
    io.appendMoveLeft();
    io.append("a");
    io.append("\r\n");
    assertLine("a");
  }

  public void testMoveUp() throws Exception {
    term.addToHistory("foo");
    io.appendMoveUp();
    io.append("\r\n");
    assertLine("foo");
  }

  public void testMoveUpDuringEdition() throws Exception {
    term.addToHistory("foo");
    io.append("bar");
    io.appendMoveLeft();
    io.appendMoveUp();
    io.append("\r\n");
    assertLine("foo");
  }

  public void testIdempotentMoveUp() throws Exception {
    term.addToHistory("foo");
    io.appendMoveUp();
    io.appendMoveUp();
    io.append("\r\n");
    assertLine("foo");
  }

  public void testIdempotentMoveDown() throws Exception {
    term.addToHistory("foo");
    io.appendMoveDown();
    io.append("\r\n");
    assertLine("");
  }

  public void testTab() throws Exception {
    io.appendTab();
    assertComplete("");
    io.append("a");
    io.appendTab();
    assertComplete("a");
    io.append("b");
    io.appendTab();
    assertComplete("ab");
    io.appendMoveLeft();
    io.appendTab();
    assertComplete("a");
  }

  public void testBufferInsert() throws Exception {
    io.append("a");
    io.moveLeft();
    term.getInsertBuffer().append('b');
    io.append("\r\n");
    assertLine("ba");
  }
}
