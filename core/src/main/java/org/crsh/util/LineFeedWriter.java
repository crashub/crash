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

import org.crsh.shell.ShellAppendable;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class LineFeedWriter extends FilterWriter implements ShellAppendable {

  /** . */
  private char[] tmp;

  /** . */
  private final String lineFeed;

  /** . */
  private String padding;

  /** . */
  private boolean padded;

  public LineFeedWriter(Writer out) {
    this(out, "\r\n");
  }

  public LineFeedWriter(Writer out, String lineFeed) {
    super(out);

    //
    this.lineFeed = lineFeed;
    this.padding = null;
    this.padded = false;
  }

  public String getPadding() {
    return padding;
  }

  public void setPadding(String padding) {
    if (padding != null) {
      for (int i = 0;i < padding.length();i++) {
        char c = padding.charAt(i);
        if (c == '\r' || c == '\n') {
          throw new IllegalArgumentException("Padding must not contain the char with code " + (int)c);
        }
      }
    }

    //
    this.padding = padding;
  }

  @Override
  public void write(int c) throws IOException {
    if (tmp == null) {
      tmp = new char[1];
    }
    tmp[0] = (char)c;
    write(tmp, 0, 1);
  }

  @Override
  public void write(char[] cbuf, int off, int len) throws IOException {
    int previous = off;
    int to = off + len;
    for (int i = off;i < to;i++) {
      char c = cbuf[i];
      if (c == '\r') {
        if (i > previous) {
          realWrite(cbuf, previous, i - previous);
        }
        previous = i + 1;
      } else if (c == '\n') {
        if (i > previous) {
          realWrite(cbuf, previous, i - previous);
        }
        writeLF();
        previous = i + 1;
        i++;
      }
    }
    if (to != previous) {
      realWrite(cbuf, previous, to - previous);
    }
  }


  @Override
  public void write(String str, int off, int len) throws IOException {
    int previous = off;
    int to = off + len;
    for (int i = off;i < to;i++) {
      char c = str.charAt(i);
      if (c == '\r') {
        if (i > previous) {
          realWrite(str, previous, i - previous);
        }
        previous = i + 1;
      } else if (c == '\n') {
        if (i > previous) {
          realWrite(str, previous, i - previous);
        }
        writeLF();
        previous = i + 1;
        i++;
      }
    }
    if (to != previous) {
      realWrite(str, previous, to  - previous);
    }
  }

  private void realWrite(String str, int off, int len) throws IOException {
    if (len > 0) {
      if (!padded) {
        if (padding != null) {
          out.write(padding);
        }
        padded = true;
      }
      out.write(str, off, len);
    }
  }

  private void realWrite(char[] cbuf, int off, int len) throws IOException {
    if (len > 0) {
      if (!padded) {
        if (padding != null) {
          out.write(padding);
        }
        padded = true;
      }
      out.write(cbuf, off, len);
    }
  }

  private void writeLF() throws IOException {
    out.write(lineFeed);
    padded = false;
  }
}
