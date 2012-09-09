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

package org.crsh.shell.io;

import org.crsh.command.InvocationContext;
import org.crsh.shell.ui.Element;
import org.crsh.text.ShellPrintWriter;
import org.crsh.shell.ui.UIBuilder;

import java.io.Closeable;
import java.io.Flushable;

public class ShellPrinter extends ShellPrintWriter {

  /** . */
  private final ShellFormatter out;

  /** . */
  private final InvocationContext context;

  public ShellPrinter(ShellFormatter out, Flushable flushable, Closeable closeable, InvocationContext context) {
    super(out, flushable, closeable);

    //
    this.out = out;
    this.context = context;
  }

  @Override
  public void println(Object x) {
    print(x);
    println();
  }

  @Override
  public void print(Object obj) {
    if (obj instanceof UIBuilder) {
      for (Element element : ((UIBuilder)obj).getElements()) {
        print(element);
      }
    } else if (obj instanceof Element) {
      ((Element)obj).print(out, context);
    } else {
      super.print(obj);
    }
  }
}
