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

package org.crsh.text.ui;

import org.crsh.command.InvocationContext;
import org.crsh.text.ShellAppendable;
import org.crsh.text.ShellPrintWriter;

import java.io.Closeable;
import java.io.Flushable;

public class UIPrinterWriter extends ShellPrintWriter {

  /** . */
  private final ShellAppendable out;

  /** . */
  private final InvocationContext context;

  public UIPrinterWriter(ShellAppendable out, Flushable flushable, Closeable closeable, InvocationContext context) {
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
      int width = context.getWidth();
      Renderer renderer = ((Element)obj).renderer(width);
      if (renderer != null) {
        while (renderer.hasLine()) {
          renderer.renderLine(new RendererAppendable(out));
          out.append('\n');
        }
      }
    } else {
      super.print(obj);
    }
  }
}
