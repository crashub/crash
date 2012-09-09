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

package org.crsh.shell.io;

import org.crsh.text.ChunkSequence;
import org.crsh.text.ShellAppendable;
import org.crsh.text.Style;

import java.io.IOException;

public class ShellWriter implements ShellAppendable {

  /** . */
  private static final int NOT_PADDED = 0;

  /** . */
  private static final int PADDING = 1;

  /** . */
  private static final int PADDED = 2;

  /** . */
  private final ChunkSequence reader;

  /** . */
  private final String lineFeed;

  /** . */
  private int status;

  public ShellWriter(ChunkSequence reader) {
    this(reader, "\r\n");
  }

  public ShellWriter(ChunkSequence reader, String lineFeed) {
    this.reader = reader;
    this.lineFeed = lineFeed;
    this.status = NOT_PADDED;
  }

  public Appendable append(char c) throws IOException {
    return append(null, c);
  }

  public ShellWriter append(ShellWriterContext ctx, final char c) throws IOException {
    return append(ctx, Character.toString(c));
  }

  public ShellWriter append(final Style d) {
    reader.append(d);
    return this;
  }

  public Appendable append(CharSequence csq, int start, int end) throws IOException {
    return append(null, csq, start, end);
  }

  public Appendable append(CharSequence csq) throws IOException {
    return append(null, csq);
  }

  public ShellWriter append(ShellWriterContext ctx, CharSequence csq) throws IOException {
    return append(ctx, csq, 0, csq.length());
  }

  public ShellWriter append(ShellWriterContext ctx, CharSequence csq, int start, int end) throws IOException {
    int previous = start;
    int to = start + end;
    for (int i = start;i < to;i++) {
      char c = csq.charAt(i);
      if (c == '\r') {
        if (i > previous) {
          realAppend(ctx, csq, previous, i);
        }
        previous = i + 1;
      } else if (c == '\n') {
        if (i > previous) {
          realAppend(ctx, csq, previous, i);
        }
        writeLF(ctx);
        previous = i + 1;
        i++;
      }
    }
    if (to != previous) {
      realAppend(ctx, csq, previous, to);
    }
    return this;
  }

  private void realAppend(ShellWriterContext ctx, CharSequence csq, int off, int end) throws IOException {
    if (end > off) {

      //
      switch (status) {
        case NOT_PADDED:
          if (ctx != null) {
            status = PADDING;
            ctx.pad(this);
          }
          status = PADDED;
          break;
        case PADDING:
        case PADDED:
          // Do nothing
          break;
        default:
          throw new AssertionError();
      }

      //
      reader.append(csq.subSequence(off, end).toString());

      //
      switch (status) {
        case PADDING:
          // Do nothing
          break;
        case PADDED:
          if (ctx != null) {
            ctx.text(csq, off, end);
          }
          break;
        default:
          throw new AssertionError();
      }
    }
  }

  private void writeLF(ShellWriterContext ctx) throws IOException {
    switch (status) {
      case PADDING:
        throw new IllegalStateException();
      case PADDED:
        status = NOT_PADDED;
      case NOT_PADDED:
        reader.append(lineFeed);
        if (ctx != null) {
          ctx.lineFeed();
        }
        break;
      default:
        throw new AssertionError();
    }
  }

  public boolean isEmpty() {
    return reader.isEmpty();
  }

  public ShellWriter cls() {
    reader.cls();
    return this;
  }
}
