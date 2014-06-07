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

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;

public class ScreenBuffer implements Iterable<Object>, Serializable, Screenable {

  /** . */
  private final LinkedList<Object> chunks;

  /** . */
  private Style current;

  /** . */
  private Style next;

  /** Where we flush. */
  private final ScreenContext out;

  public ScreenBuffer() {
    this.chunks = new LinkedList<Object>();
    this.current = Style.style();
    this.next = Style.style();
    this.out = null;
  }

  public ScreenBuffer(ScreenContext out) {
    this.chunks = new LinkedList<Object>();
    this.current = Style.style();
    this.next = Style.style();
    this.out = out;
  }

  public Iterator<Object> iterator() {
    return chunks.iterator();
  }

  public void format(Format format, Appendable appendable) throws IOException {
    format.begin(appendable);
    for (Object chunk : this) {
      if (chunk instanceof Style) {
        format.write((Style)chunk, appendable);
      } else if (chunk instanceof CLS) {
        format.cls(appendable);
      } else {
        format.write((CharSequence)chunk, appendable);
      }
    }
    format.end(appendable);
  }

  public ScreenBuffer append(Iterable<?> data) throws NullPointerException {
    for (Object o : data) {
      append(o);
    }
    return this;
  }

  public ScreenBuffer append(Object... data) throws NullPointerException {
    for (Object o : data) {
      append(o);
    }
    return this;
  }

  public ScreenBuffer cls() {
    chunks.addLast(CLS.INSTANCE);
    return this;
  }

  public ScreenBuffer append(Style style) throws NullPointerException {
    next = next.merge(style);
    return this;
  }

  @Override
  public ScreenBuffer append(char c) throws IOException {
    return append(Character.toString(c));
  }

  public ScreenBuffer append(CharSequence s) {
    if (s.length() > 0) {
      if (!next.equals(current)) {
        if (!Style.style().equals(next)) {
          chunks.addLast(next);
        }
        current = next;
        next = Style.style();
      }
      chunks.addLast(s);
    }
    return this;
  }

  public ScreenBuffer append(CharSequence s, int start, int end) {
    if (end != start) {
      if (start != 0 || end != s.length()) {
        s = s.subSequence(start, end);
      }
      append(s);
    }
    return this;
  }

  public void flush() throws IOException {
    if (out != null) {
      for (Object chunk : chunks) {
        if (chunk instanceof CLS) {
          out.cls();
        } else if (chunk instanceof CharSequence) {
          out.append((CharSequence)chunk);
        } else {
          out.append((Style)chunk);
        }
      }
    }
    chunks.clear();
    if (out != null) {
      out.flush();
    }
  }

  public ScreenBuffer append(ScreenBuffer s) throws NullPointerException {
    for (Object chunk : s.chunks) {
      append(chunk);
    }
    if (s.next != null && !s.next.equals(Style.style())) {
      append(s.next);
    }
    return this;
  }

  public ScreenBuffer append(Object o) throws NullPointerException {
    if (o == null) {
      throw new NullPointerException("No null accepted");
    }
    if (o instanceof ScreenBuffer) {
      append((ScreenBuffer)o);
    } else if (o instanceof Style) {
      append((Style)o);
    } else if (o instanceof CharSequence){
      append(((CharSequence)o));
    } else if (o instanceof CLS) {
      cls();
    } else {
      append(o.toString());
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
    if (obj instanceof ScreenBuffer) {
      ScreenBuffer that = (ScreenBuffer)obj;
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
