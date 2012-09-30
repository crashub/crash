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

import org.crsh.shell.io.ShellFormatter;

import java.util.ArrayList;
import java.util.List;

public class TreeElement extends Element {

  /** An optional value element. */
  private Element value;

  /** . */
  private List<Element> children = new ArrayList<Element>();

  public TreeElement() {
    this(null);
  }

  public TreeElement(Element value) {
    this.value = value;
  }

  public TreeElement addChild(Element child) {
    if (child.parent != null) {
      throw new IllegalArgumentException("Child has already a parent");
    }
    children.add(child);
    child.parent =  this;
    return this;
  }

  public int getSize() {
    return children.size();
  }

  public Element getValue() {
    return value;
  }

  public Element getNode(int index) {
    return children.get(index);
  }

  @Override
  void doPrint(UIWriterContext ctx, ShellFormatter writer) {

    //
    if (value != null) {
      value.print(ctx, writer);
      writer.append(ctx, '\n');
    }

    //
    for (int i = 0;i < children.size();i++) {

      // Add the padding ?
      ctx.stack.add(i == children.size() - 1 ? Pad.LAST_BRANCH : Pad.BRANCH);
//      ctx.stack.add(Foo.TRUE);

      //
      Element node = children.get(i);
      node.print(ctx, writer);
      if (ctx.needLF) {
        writer.append(ctx, '\n');
      }
      ctx.stack.remove(ctx.stack.size() - 1);
    }
  }

  @Override
  int width() {
    return 0;
  }
}
