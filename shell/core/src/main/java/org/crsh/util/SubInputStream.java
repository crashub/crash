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

package org.crsh.util;

import java.io.IOException;
import java.io.InputStream;

public class SubInputStream extends InputStream {

  /** . */
  private final InputStream in;

  /** . */
  private final long length;

  /** . */
  private long count;

  public SubInputStream(InputStream in, long length) {
    if (in == null) {
      throw new NullPointerException("Stream cannot be null");
    }
    if (length < 0) {
      throw new IllegalArgumentException("Length cannot be negative");
    }

    //
    this.in = in ;
    this.length = length;
    this.count = 0;
  }

  @Override
  public int read() throws IOException {
    if (count < length) {
      int value = in.read();
      count++;
      return value;
    } else {
      return -1;
    }
  }
}
