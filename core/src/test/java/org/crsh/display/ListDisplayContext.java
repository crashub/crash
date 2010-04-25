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

import java.util.LinkedList;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ListDisplayContext extends DisplayContext {

  /** . */
  final LinkedList<String> lines = new LinkedList<String>();

  public ListDisplayContext() {
    lines.add("");
  }

  @Override
  protected void print(char[] cbuf, int off, int len) {
    String s = lines.removeLast();
    s = s + new String(cbuf, off, len);
    lines.addLast(s);
  }

  @Override
  protected void println() {
    lines.add("");
  }
}
