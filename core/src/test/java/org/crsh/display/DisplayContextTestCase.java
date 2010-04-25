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
package org.crsh.display;

import junit.framework.TestCase;

import java.io.PrintWriter;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class DisplayContextTestCase extends TestCase {

  /** . */
  private ListDisplayContext ctx;

  /** . */
  private DisplayWriter writer;

  /** . */
  private PrintWriter printer;

  @Override
  protected void setUp() throws Exception {
    ctx = new ListDisplayContext();
    writer = new DisplayWriter(ctx);
    printer = writer.getPrinter();
  }

  public void testPrint1() {
    printer.print("a\nb");
    assertEquals("a", ctx.lines.removeFirst());
    assertEquals("b", ctx.lines.removeFirst());
    assertTrue(ctx.lines.isEmpty());
  }

  public void testPrint2() {
    printer.print("");
    assertEquals("", ctx.lines.removeFirst());
    assertTrue(ctx.lines.isEmpty());
  }

  public void testPrint3() {
    printer.print("\n");
    assertEquals("", ctx.lines.removeFirst());
    assertEquals("", ctx.lines.removeFirst());
    assertTrue(ctx.lines.isEmpty());
  }

  public void testPrint4() {
    printer.print("a\n");
    assertEquals("a", ctx.lines.removeFirst());
    assertEquals("", ctx.lines.removeFirst());
    assertTrue(ctx.lines.isEmpty());
  }

  public void testPrint5() {
    printer.print("\na");
    assertEquals("", ctx.lines.removeFirst());
    assertEquals("a", ctx.lines.removeFirst());
    assertTrue(ctx.lines.isEmpty());
  }
}
