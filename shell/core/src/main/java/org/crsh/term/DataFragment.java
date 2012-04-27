package org.crsh.term;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class DataFragment {

  /** . */
  private CharSequence value;

  public DataFragment(CharSequence value) {
    this.value = value;
  }

  public CharSequence get() {
    return value;
  }
}
