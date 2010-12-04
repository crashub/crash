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
package org.crsh.display.structure;

import org.crsh.display.DisplayContext;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TreeElement extends Element {

  /** An optional value element. */
  private Element value;

  /** . */
  private List<Element> nodes = new ArrayList<Element>();

  public TreeElement() {
    this(null);
  }

  public TreeElement(Element value) {
    this.value = value;
  }

  public TreeElement addNode(Element node) {
    nodes.add(node);
    return this;
  }

  public int getSize() {
    return nodes.size();
  }

  public Element getValue() {
    return value;
  }

  public Element getNode(int index) {
    return nodes.get(index);
  }

  private static class DisplayContextImpl extends DisplayContext {

    /** . */
    private static final char[] firstRow = "+-".toCharArray();

    /** . */
    private static final char[] otherRow = "| ".toCharArray();

    /** . */
    private final PrintWriter parent;

    /** . */
    private boolean first = true;

    /** . */
    private boolean padded = false;

    private DisplayContextImpl(PrintWriter parent) {
      this.parent = parent;
    }

    private void pad() {
      if (!padded) {
        if (first) {
          parent.write(firstRow, 0, 2);
          first = false;
        } else {
          parent.write(otherRow, 0, 2);
        }
        padded = true;
      }
    }

    @Override
    protected void print(char[] cbuf, int off, int len){
      pad();
      parent.write(cbuf, off, len);
    }

    @Override
    protected void println()  {
      if (padded) {
        parent.println();
        padded = false;
      }
    }
  }

  @Override
  public void print(PrintWriter printer) {
    if (value != null) {
      value.print(printer);
      printer.println();
    }
    DisplayContextImpl nodeContext = new DisplayContextImpl(printer);
    for (Element node : nodes) {
      node.print(nodeContext);
      nodeContext.printer().println();
      nodeContext.first = true;
    }
  }
}
