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
package org.crsh.display.structure;

import org.crsh.display.DisplayContext;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class LabelElement extends Element {

  /** . */
  private final String value;

  public LabelElement(String value) {
    this.value = value;
  }

  public LabelElement(Object value) {
    this.value = String.valueOf(value);
  }

  public String getValue() {
    return value;
  }

  @Override
  public void print(DisplayContext context){
    char[] cbuf = value.toCharArray();
    context.write(cbuf, 0, cbuf.length);
  }
}

