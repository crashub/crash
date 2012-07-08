package org.crsh.text;

import java.io.Serializable;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class DataFragment implements Serializable {

  /** . */
  private CharSequence value;

  public DataFragment(CharSequence value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return this.value.toString();
  }

}
