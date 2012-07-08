package org.crsh.text;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class Data implements Iterable<Object>, Serializable {

  /** . */
  private final LinkedList<Object> fragments;

  public Data() {
    this.fragments = new LinkedList<Object>();
  }

  public Data(CharSequence data) {
    this(new DataFragment(data));
  }

  public Data(DataFragment fragment) {
    this();
    fragments.add(fragment);
  }

  public Iterator<Object> iterator() {
    return fragments.iterator();
  }

  public void writeAnsi(PrintWriter writer) {
    for (Object f : fragments) {
      if (f instanceof FormattingData) {
        ((FormattingData)f).writeAnsi(writer);
      } else {
        writer.append(f.toString());
      }
    }
  }

  public void writeAnsi(Appendable appendable) throws IOException {
    for (Object f : fragments) {
      if (f instanceof FormattingData) {
        ((FormattingData)f).writeAnsi(appendable);
      } else {
        appendable.append(f.toString());
      }
    }
  }

  public void append(Data data) {
    fragments.addAll(data.fragments);
  }

  public void append(DataFragment fragment) {
    fragments.add(fragment);
  }

  public boolean contains(Object o) {
    return toString().contains(o.toString());
  }

  public boolean isEmpty() {
    return fragments.isEmpty();
  }

  public void clear() {
    fragments.clear();
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
    if (obj instanceof Data) {
      Data that = (Data)obj;
      return toString().equals(that.toString());
    }
    return false;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (Object fragment : fragments) {
      sb.append(fragment.toString());
    }
    return sb.toString();
  }
}
