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

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;

public class AppendableWriter extends Writer {

  /** . */
  private final Appendable out;

  /** . */
  private final Flushable flushable;

  /** . */
  private final Closeable closeable;

  /** . */
  private boolean closed;

  /** . */
  private boolean empty;

  public AppendableWriter(Appendable out) throws NullPointerException {
    this(out, null, null);
  }

  public AppendableWriter(Appendable out, Flushable flushable) throws NullPointerException {
    this(out, flushable, null);
  }

  public AppendableWriter(Appendable out, Closeable closeable) throws NullPointerException {
    this(out, null, closeable);
  }

  public AppendableWriter(Appendable out, Flushable flushable, Closeable closeable) throws NullPointerException {
    if (out == null) {
      throw new NullPointerException("No null appendable expected");
    }

    //
    this.out = out;
    this.empty = true;
    this.flushable = flushable;
    this.closeable = closeable;
  }

  public boolean isEmpty() {
    return empty;
  }

  @Override
  public void write(char[] cbuf, int off, int len) throws IOException {
    if (closed) {
      throw new IOException("Already closed");
    }
    if (len > 0) {
      empty = false;
      int end = off + len;
      while (off < end) {
        out.append(cbuf[off++]);
      }
    }
  }

  @Override
  public void flush() throws IOException {
    if (closed) {
      throw new IOException("Already closed");
    }
    if (flushable != null) {
      flushable.flush();
    }
  }

  @Override
  public void close() throws IOException {
    if (!closed) {
      closed = true;
      if (closeable != null) {
        closeable.close();
      }
    }
  }
}
