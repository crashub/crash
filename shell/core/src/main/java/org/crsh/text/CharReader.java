package org.crsh.text;

import org.crsh.util.Safe;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class CharReader implements Iterable<Object>, Serializable {

  private static class Chunk implements Serializable {

    /** . */
    Style style;

    /** . */
    StringBuilder buffer = new StringBuilder();

    private Chunk(Style style) {
      this.style = style;
    }
  }

  /** . */
  private final LinkedList<Chunk> chunks;

  /** . */
  private Style style;

  public CharReader() {
    this.chunks = new LinkedList<Chunk>();
    this.style = null;
  }

  public CharReader(CharSequence s) {
    this();

    //
    append(s);
  }

  public Iterator<Object> iterator() {
    final Iterator<Chunk> i = chunks.iterator();
    return new Iterator<Object>() {
      Style style;
      StringBuilder buffer;
      public boolean hasNext() {
        if (style != null || buffer != null) {
          return true;
        } else if (i.hasNext()) {
          Chunk next = i.next();
          style = next.style;
          buffer = next.buffer;
          return true;
        } else {
          return false;
        }
      }
      public Object next() {
        if (hasNext()) {
          Object next;
          if (style != null) {
            next = style;
            style = null;
          } else {
            next = buffer;
            buffer = null;
          }
          return next;
        } else {
          throw new NoSuchElementException();
        }
      }
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  public void writeAnsiTo(PrintWriter writer) {
    for (Chunk f : chunks) {
      if (f.style != null) {
        try {
          f.style.writeAnsiTo(writer);
        }
        catch (IOException ignore) {
        }
      }
      writer.append(f.buffer);
    }
  }

  public void writeAnsiTo(Appendable appendable) throws IOException {
    for (Chunk f : chunks) {
      if (f.style != null) {
        f.style.writeAnsiTo(appendable);
      }
      appendable.append(f.buffer);
    }
  }

  public CharReader append(Object data) throws NullPointerException {
    if (data == null) {
      throw new NullPointerException("No null accepted");
    }
    if (data instanceof CharReader) {
      append(Style.RESET);
      for (Chunk chunk : ((CharReader)data).chunks) {
        if (chunk.style != null) {
          append(chunk.style);
        }
        append(chunk.buffer);
      }
    } else {
      if (data instanceof Style) {
        style = (Style)data;
      } else {
        CharSequence s;
        if (data instanceof CharSequence) {
          s = (CharSequence)data;
        } else {
          s = data.toString();
        }
        if (s.length() > 0) {
          Chunk chunk;
          if (chunks.size() > 0) {
            Chunk last = chunks.peekLast();
            if (Safe.equals(last.style, style)) {
              chunk = last;
            } else {
              chunks.addLast(chunk = new Chunk(style));
            }
          } else {
            chunks.addLast(chunk = new Chunk(style));
          }
          chunk.buffer.append(s);
        }
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
    if (obj instanceof CharReader) {
      CharReader that = (CharReader)obj;
      return toString().equals(that.toString());
    }
    return false;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (Chunk chunk : chunks) {
      sb.append(chunk.buffer);
    }
    return sb.toString();
  }
}
