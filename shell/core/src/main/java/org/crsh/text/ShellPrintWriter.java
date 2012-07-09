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

package org.crsh.text;

import org.crsh.util.AppendableWriter;

import java.io.PrintWriter;

/**
 * The shell printer extends the {@link java.io.PrintWriter} and provides support for decorating a char stream.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ShellPrintWriter extends PrintWriter {

  /** . */
  private final ShellAppendable out;

  public ShellPrintWriter(ShellAppendable out) {
    super(new AppendableWriter(out));

    //
    this.out = out;
  }

  public final void print(Object obj, Color foreground) {
    out.append(foreground.style);
    print(obj);
    out.append(Style.reset);
  }

  public final void println(Object obj, Color foreground) {
    print(obj, foreground.style);
    println();
  }

  public final void print(Object obj, Color foreground, Color background) {
    out.append(Style.style(foreground, background));
    print(obj);
    out.append(Style.reset);
  }

  public final void println(Object obj, Color foreground, Color background) {
    print(obj, Style.style(foreground, background));
    println();
  }

  public final void print(Object obj, Decoration decoration) {
    out.append(decoration.style);
    print(obj);
    out.append(Style.reset);
  }

  public final void println(Object obj, Decoration decoration) {
    print(obj, decoration.style);
    println();
  }

  public final void print(Object obj, Decoration decoration, Color foreground) {
    print(obj, Style.style(decoration, foreground));
    println();
  }

  public final void println(Object obj, Decoration decoration, Color foreground) {
    print(obj, Style.style(decoration, foreground, null));
    println();
  }

  public final void print(Object obj, Decoration decoration, Color foreground, Color background) {
    print(obj, Style.style(decoration, foreground, background));
    println();
  }

  public final void println(Object obj, Decoration decoration, Color foreground, Color background) {
    print(obj, Style.style(decoration, foreground, background));
    println();
  }

  public final void print(Object obj, Style style) {
    out.append(style);
    print(obj);
    out.append(Style.reset);
  }

  public final void println(Object obj, Style style) {
    print(obj, style);
    println();
  }

  /**
   * Groovy left shift operator.
   *
   * @param o the appended
   * @return this object
   */
  public final ShellPrintWriter leftShift(Object o) {
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
