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

package org.crsh.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class AppendableWriter extends Writer {

  /** . */
  private final Appendable out;

  /** . */
  private boolean closed;

  public AppendableWriter(Appendable out) {
    this.out = out;
  }

  @Override
  public void write(char[] cbuf, int off, int len) throws IOException {
    if (closed) {
      throw new IOException("Already closed");
    }
    int end = off + len;
    while (off < end) {
      out.append(cbuf[off++]);
    }
  }

  @Override
  public void flush() throws IOException {
    if (closed) {
      throw new IOException("Already closed");
    }
  }

  @Override
  public void close() throws IOException {
    if (!closed) {
      closed = true;
      if (out instanceof Closeable) {
        ((Closeable)out).close();
      }
    }
  }
}
