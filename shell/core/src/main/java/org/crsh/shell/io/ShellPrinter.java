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

package org.crsh.shell.io;

import org.crsh.command.InvocationContext;
import org.crsh.text.Decoration;
import org.crsh.shell.ui.Element;
import org.crsh.text.Color;
import org.crsh.text.Style;
import org.crsh.shell.ui.UIBuilder;
import org.crsh.util.AppendableWriter;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * The shell printer extends the {@link PrintWriter} and prints some objects in a special
 * manner.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ShellPrinter extends PrintWriter {

  /** . */
  private final ShellWriter out;

  /** . */
  private final InvocationContext context;

  public ShellPrinter(ShellWriter out, InvocationContext context) {
    super(new AppendableWriter(out));

    //
    this.out = out;
    this.context = context;
  }

  @Override
  public void println(Object x) {
    print(x);
    println();
  }

  public void print(Object obj, Color foreground) {
    out.append(foreground.style);
    print(obj);
  }

  public void println(Object obj, Color foreground) {
    out.append(foreground.style);
    println(obj);
  }

  public void print(Object obj, Color background, Color foreground) {
    out.append(Style.create(background, foreground));
    print(obj);
  }

  public void println(Object obj, Color background, Color foreground) {
    out.append(Style.create(background, foreground));
    println(obj);
  }

  public void print(Object obj, Decoration decoration) {
    out.append(decoration.style);
    print(obj);
  }

  public void println(Object obj, Decoration decoration) {
    out.append(decoration.style);
    println(obj);
  }

  public void print(Object obj, Decoration decoration, Color foreground) {
    out.append(Style.create(decoration, foreground));
    print(obj);
  }

  public void println(Object obj, Decoration decoration, Color foreground) {
    out.append(Style.create(decoration, null, foreground));
    println(obj);
  }

  public void print(Object obj, Decoration decoration, Color background, Color foreground) {
    out.append(Style.create(decoration, background, foreground));
    print(obj);
  }

  public void println(Object obj, Decoration decoration, Color background, Color foreground) {
    out.append(Style.create(decoration, background, foreground));
    println(obj);
  }

  public void print(Object obj, Style style) {
    out.append(style);
    print(obj);
  }

  public void println(Object obj, Style style) {
    out.append(style);
    println(obj);
  }

  @Override
  public void print(Object obj) {
    if (obj instanceof UIBuilder) {
      for (Element element : ((UIBuilder)obj).getElements()) {
        print(element);
      }
    } else if (obj instanceof Element) {
      try {
        ((Element)obj).print(out, context);
      } catch (IOException e) {
        setError();
      }
    } else {
      super.print(obj);
    }
  }

  /**
   * Groovy left shift operator.
   *
   * @param o the appended
   * @return this object
   */
  public ShellPrinter leftShift(Object o) {
    if (o instanceof Style) {
      out.append((Style)o);
    } else if (o instanceof Decoration) {
      out.append(((Decoration)o).style);
    } else if (o instanceof Color) {
      out.append(((Color)o).style);
    } else {
      print(o);
    }
    return this;
  }
}
