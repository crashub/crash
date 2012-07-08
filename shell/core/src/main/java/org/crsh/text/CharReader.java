package org.crsh.text;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class CharReader implements Iterable<Object>, Serializable {

  /** . */
  private final LinkedList<Object> stream;

  public CharReader() {
    this.stream = new LinkedList<Object>();
  }

  public CharReader(CharSequence s) {
    this();

    //
    this.stream.add(s);
  }

  public Iterator<Object> iterator() {
    return stream.iterator();
  }

  public void writeAnsi(PrintWriter writer) {
    for (Object f : stream) {
      if (f instanceof Style) {
        try {
          ((Style)f).writeAnsiTo(writer);
        }
        catch (IOException ignore) {
        }
      } else {
        writer.append(f.toString());
      }
    }
  }

  public void writeAnsi(Appendable appendable) throws IOException {
    for (Object f : stream) {
      if (f instanceof Style) {
        ((Style)f).writeAnsiTo(appendable);
      } else {
        appendable.append(f.toString());
      }
    }
  }

  public void append(Object data) {
    if (data instanceof CharReader) {
      this.stream.addAll(((CharReader)data).stream);
    } else {
      this.stream.add(data);
    }
  }

  public boolean contains(Object o) {
    return toString().contains(o.toString());
  }

  public boolean isEmpty() {
    return stream.isEmpty();
  }

  public void clear() {
    stream.clear();
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
    for (Object fragment : stream) {
      if (fragment instanceof Style) {
        //
      } else {
        sb.append(fragment.toString());
      }
    }
    return sb.toString();
  }
}
