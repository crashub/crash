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

import org.crsh.shell.ui.Element;
import org.crsh.shell.ui.FormattingElement;
import org.crsh.shell.ui.Style;
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

  public ShellPrinter(ShellWriter out) {
    super(new AppendableWriter(out));

    //
    this.out = out;
  }

  @Override
  public void println(Object x) {
    print(x);
    println();
  }

  public void print(Object obj, Style style) {
    print(new FormattingElement(style));
    print(obj);
    print(new FormattingElement(null));

  }

  public void println(Object obj, Style style) {
    print(obj);
    println();
  }

  @Override
  public void print(Object obj) {
    if (obj instanceof UIBuilder) {
      for (Element element : ((UIBuilder)obj).getElements()) {
        print(element);
      }
    } else if (obj instanceof Element) {
      try {
        ((Element)obj).print(out);
      } catch (IOException e) {
        setError();
      }
    } else {
      super.print(obj);
    }
  }

}
