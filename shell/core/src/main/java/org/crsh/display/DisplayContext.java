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
package org.crsh.display;

import java.io.PrintWriter;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class DisplayContext {

  /** . */
  private DisplayWriter writer;

  public final void write(char[] cbuf, int off, int len){
    int previous = off;
    int to = off + len;
    for (int i = off;i < to;i++) {
      if (cbuf[i] == '\n') {
        if (i > previous) {
          print(cbuf, previous, i - previous);
        }
        println();
        previous = i + 1;
        i++;
      }
    }
    if (to != previous) {
      print(cbuf, previous, to - previous);
    }
  }

  public PrintWriter printer() {
    if (writer == null) {
      writer = new DisplayWriter(this);
    }
    return writer.getPrinter();
  }

  protected abstract void print(char[] cbuf, int off, int len);

  protected abstract void println();

}
