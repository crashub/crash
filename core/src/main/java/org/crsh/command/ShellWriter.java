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

package org.crsh.command;

import org.crsh.display.DisplayBuilder;
import org.crsh.display.DisplayContext;
import org.crsh.display.structure.Element;

import java.io.PrintWriter;
import java.io.Writer;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ShellWriter extends PrintWriter {

  private final DisplayContext dc = new DisplayContext() {
    @Override
    protected void print(char[] cbuf, int off, int len) {

    }

    @Override
    protected void println() {
      ShellWriter.super.print(lineFeed);
    }
  };

  /** . */
  private final String lineFeed;

  public ShellWriter(Writer out, String lineFeed) {
    super(out);

    //
    this.lineFeed = lineFeed;
  }

  @Override
  public void print(Object obj) {

    //
    if (obj instanceof DisplayBuilder) {
      for (Element element : ((DisplayBuilder)obj).getElements()) {
        print(element);
      }
    } else if (obj instanceof Element) {
      ((Element)obj).print(this);
    } else {
      super.print(obj);
    }
  }
}
