package org.crsh.term;

import java.util.LinkedList;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class Data extends LinkedList<DataFragment> {

  public Data() {
  }

  public Data(CharSequence data) {
    super.add(new DataFragment(data));
  }

  public Data(DataFragment fragment) {
    super.add(fragment);
  }

  @Override
  public boolean contains(Object o) {
    return toString().contains(o.toString());
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
    StringBuffer sb = new StringBuffer();

    for (DataFragment fragment : this) {
      sb.append(fragment.toString());
    }

    return sb.toString();
  }
}
