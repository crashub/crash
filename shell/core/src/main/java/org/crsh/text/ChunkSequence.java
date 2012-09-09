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

package org.crsh.text;

import org.crsh.util.Safe;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;

public class ChunkSequence implements Iterable<Chunk>, Serializable {

  /** . */
  private final LinkedList<Chunk> chunks;

  /** . */
  private Style previousStyle;

  /** . */
  private Style currentStyle;

  public ChunkSequence() {
    this.chunks = new LinkedList<Chunk>();
    this.previousStyle = null;
    this.currentStyle = null;
  }

  public ChunkSequence(CharSequence s) {
    this();

    //
    append(s);
  }

  public Iterator<Chunk> iterator() {
    return chunks.iterator();
  }

  @Deprecated
  public void writeAnsiTo(PrintWriter writer) {
    try {
      writeAnsiTo((Appendable)writer);
    }
    catch (IOException ignore) {
    }
  }

  @Deprecated
  public void writeAnsiTo(Appendable appendable) throws IOException {
    Iterator<Chunk> iterator = iterator();
    while (iterator.hasNext()) {
      Chunk chunk = iterator.next();
      if (chunk instanceof TextChunk) {
        TextChunk text = (TextChunk)chunk;
        if (text.buffer.length() > 0) {
          appendable.append(text.buffer);
        }
      } else if (chunk instanceof Style) {
        ((Style)chunk).writeAnsiTo(appendable);
      }
    }
  }

  public ChunkSequence append(Object... data) throws NullPointerException {
    for (Object o : data) {
      append(o);
    }
    return this;
  }

  public void cls() {
    chunks.addLast(new CLS());
  }

  public ChunkSequence append(Style nextStyle) throws NullPointerException {
    if (currentStyle != null) {
      if (currentStyle.equals(nextStyle)) {
        // Do nothing
      } else {
        currentStyle = currentStyle.merge(nextStyle);
      }
    } else {
      currentStyle = nextStyle;
    }
    return this;
  }

  public ChunkSequence append(CharSequence s) throws NullPointerException {
    if (s.length() > 0) {
      TextChunk chunk;

      // See if we can merge it in the last chunk
      if (chunks.size() > 0 && chunks.peekLast() instanceof TextChunk) {
        if (Safe.equals(previousStyle, currentStyle)) {
          chunk = (TextChunk)chunks.peekLast();
        } else {
          chunks.addLast(currentStyle);
          chunks.addLast(chunk = new TextChunk());
          previousStyle = currentStyle;
        }
      } else {
        if (currentStyle != null) {
          previousStyle = currentStyle;
          chunks.addLast(currentStyle);
          chunks.addLast(chunk = new TextChunk());
        } else {
          chunks.addLast(chunk = new TextChunk());
        }
      }
      chunk.buffer.append(s);
    }
    return this;
  }

  public ChunkSequence append(ChunkSequence s) throws NullPointerException {
    for (Chunk chunk : s.chunks) {
      if (chunk instanceof TextChunk) {
        append(((TextChunk)chunk).buffer);
      } else if (chunk instanceof Style) {
        append(((Style)chunk));
      } else {
        cls();
      }
    }
    currentStyle = s.currentStyle;
    return this;
  }

  public ChunkSequence append(Object o) throws NullPointerException {
    if (o == null) {
      throw new NullPointerException("No null accepted");
    }
    if (o instanceof ChunkSequence) {
      append((ChunkSequence)o);
    } else {
      if (o instanceof Style) {
        append((Style)o);
      } else {
        CharSequence s;
        if (o instanceof CharSequence) {
          s = (CharSequence)o;
        } else {
          s = o.toString();
        }
        append(s);
      }
    }
    return this;
  }

  public boolean contains(Object o) {
    return toString().contains(o.toString());
  }

  public boolean isEmpty() {
    return chunks.isEmpty();
  }

  public void clear() {
    chunks.clear();
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof ChunkSequence) {
      ChunkSequence that = (ChunkSequence)obj;
      return toString().equals(that.toString());
    }
    return false;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (Chunk chunk : chunks) {
      if (chunk instanceof TextChunk) {
        sb.append(((TextChunk)chunk).buffer);
      }
    }
    return sb.toString();
  }
}
