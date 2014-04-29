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

import org.crsh.io.Consumer;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;

public class ChunkBuffer implements Iterable<Chunk>, Serializable, Consumer<Chunk> {

  /** . */
  private final LinkedList<Chunk> chunks;

  /** . */
  private Style current;

  /** . */
  private Style next;

  /** Where we flush. */
  private final Consumer<Chunk> out;

  public ChunkBuffer() {
    this.chunks = new LinkedList<Chunk>();
    this.current = Style.style();
    this.next = Style.style();
    this.out = null;
  }

  public ChunkBuffer(Consumer<Chunk> out) {
    this.chunks = new LinkedList<Chunk>();
    this.current = Style.style();
    this.next = Style.style();
    this.out = out;
  }

  public Iterator<Chunk> iterator() {
    return chunks.iterator();
  }

  public void format(Format format, Appendable appendable) throws IOException {
    for (Chunk chunk : this) {
      format.append(chunk, appendable);
    }
  }

  public ChunkBuffer append(Iterable<?> data) throws NullPointerException {
    for (Object o : data) {
      append(o);
    }
    return this;
  }

  public ChunkBuffer append(Object... data) throws NullPointerException {
    for (Object o : data) {
      append(o);
    }
    return this;
  }

  public ChunkBuffer cls() {
    chunks.addLast(CLS.INSTANCE);
    return this;
  }

  public ChunkBuffer append(Style style) throws NullPointerException {
    next = next.merge(style);
    return this;
  }

  public ChunkBuffer append(Text text) {
    CharSequence s = text.getText();
    if (s.length() > 0) {
      if (!next.equals(current)) {
        if (!Style.style().equals(next)) {
          chunks.addLast(next);
        }
        current = next;
        next = Style.style();
      }
      chunks.addLast(text);
    }
    return this;
  }

  public ChunkBuffer append(CharSequence s) {
    return append(s, 0, s.length());
  }

  public ChunkBuffer append(CharSequence s, int start, int end) {
    if (end != start) {
      if (start != 0 || end != s.length()) {
        s = s.subSequence(start, end);
      }
      append(Text.create(s));
    }
    return this;
  }

  public Class<Chunk> getConsumedType() {
    return Chunk.class;
  }

  public void provide(Chunk element) throws IOException {
    append(element);
  }

  public void flush() throws IOException {
    if (out != null) {
      for (Chunk chunk : chunks) {
        out.provide(chunk);
      }
    }
    chunks.clear();
    if (out != null) {
      out.flush();
    }
  }

  public ChunkBuffer append(ChunkBuffer s) throws NullPointerException {
    for (Chunk chunk : s.chunks) {
      write(chunk);
    }
    if (s.next != null && !s.next.equals(Style.style())) {
      write(s.next);
    }
    return this;
  }

  public void write(Chunk chunk) throws NullPointerException {
    if (chunk instanceof Style) {
      append((Style)chunk);
    } else if (chunk instanceof Text){
      append(((Text)chunk));
    } else {
      cls();
    }
  }

  public ChunkBuffer append(Object o) throws NullPointerException {
    if (o == null) {
      throw new NullPointerException("No null accepted");
    }
    if (o instanceof ChunkBuffer) {
      append((ChunkBuffer)o);
    } else if (o instanceof Chunk) {
      write((Chunk)o);
    } else {
      CharSequence s;
      if (o instanceof CharSequence) {
        s = (CharSequence)o;
      } else {
        s = o.toString();
      }
      append(s);
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
    if (obj instanceof ChunkBuffer) {
      ChunkBuffer that = (ChunkBuffer)obj;
      return toString().equals(that.toString());
    }
    return false;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    try {
      format(Format.TEXT, sb);
    }
    catch (IOException ignore) {
    }
    return sb.toString();
  }
}
