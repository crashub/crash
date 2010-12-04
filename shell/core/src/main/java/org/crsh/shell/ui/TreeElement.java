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

package org.crsh.shell.ui;

import org.crsh.shell.io.ShellWriter;

import java.io.IOException;
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

  @Override
  void print(UIWriterContext ctx, ShellWriter writer) throws IOException {
    if (ctx == null) {
      ctx = new UIWriterContext();
    }

    //
    if (value != null) {
      value.print(ctx, writer);
      writer.append(ctx, '\n');
    }

    //
    for (int i = 0;i < nodes.size();i++) {
      ctx.stack.add(Boolean.TRUE);
      Element node = nodes.get(i);
      node.print(ctx, writer);
      if (ctx.needLF) {
        writer.append(ctx, '\n');
      }
      ctx.stack.remove(ctx.stack.size() - 1);
    }
  }
}
