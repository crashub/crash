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

package org.crsh.shell.ui;

import junit.framework.TestCase;
import org.crsh.shell.TestInvocationContext;
import org.crsh.shell.io.ShellFormatter;
import org.crsh.text.ChunkBuffer;

import java.io.IOException;

public class TreeElementTestCase extends TestCase {

  public void testSimple() throws IOException {
    TreeElement elt = new TreeElement();
    elt.addNode(new LabelElement("1\n1"));
    elt.addNode(new LabelElement("2\n"));
    ChunkBuffer reader = new ChunkBuffer();
    ShellFormatter writer = new ShellFormatter(reader, "_");
    elt.print(writer, new TestInvocationContext());
    assertEquals(
        "+-1_" +
            "| 1_" +
            "+-2_"
        , reader.toString());
  }

  public void testFoo() throws Exception {
    TreeElement elt = new TreeElement();
    elt.addNode(new LabelElement("1\n1"));
    ChunkBuffer reader = new ChunkBuffer();
    ShellFormatter writer = new ShellFormatter(reader, "_");
    elt.print(writer, new TestInvocationContext());
    assertEquals(
        "+-1_" +
            "  1_"
        , reader.toString());
  }

  public void testNested() throws Exception {
    TreeElement elt = new TreeElement(new LabelElement("foo"));
    elt.addNode(new TreeElement(new LabelElement("bar")).addNode(new LabelElement("1\n1")).addNode(new LabelElement("2\n2")));
    elt.addNode(new TreeElement().addNode(new LabelElement("3")).addNode(new LabelElement("4")));
    ChunkBuffer reader = new ChunkBuffer();
    ShellFormatter writer = new ShellFormatter(reader, "_");
    elt.print(writer, new TestInvocationContext());
    assertEquals(
        "foo_" +
            "+-bar_" +
            "| +-1_" +
            "| | 1_" +
            "| +-2_" +
            "|   2_" +
            "+-+-3_" +
            "  +-4_"
        , reader.toString());
  }

  public void testTooLarge() throws IOException {
    TreeElement elt = new TreeElement();
    elt.addNode(new LabelElement("foo value is very very very too large for the console"));
    elt.addNode(new LabelElement("bar\n"));
    ChunkBuffer reader = new ChunkBuffer();
    ShellFormatter writer = new ShellFormatter(reader, "_");
    elt.print(writer, new TestInvocationContext());

    assertEquals(
        "+-foo value is very very very to_" +
        "| o large for the console       _" +
        "+-bar_"
        , reader.toString());
  }
}
