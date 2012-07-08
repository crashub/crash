package org.crsh.text;

import java.io.IOException;
import java.io.PrintWriter;
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

  public void writeAnsi(Appendable appendable) throws IOException {
    if (this instanceof FormattingData) {
      appendable.append(((FormattingData)this).asAnsiSequence());
    } else {
      appendable.append(value);
    }
  }

  public void writeAnsi(PrintWriter writer) {
    if (this instanceof FormattingData) {
      writer.append(((FormattingData)this).asAnsiSequence());
    } else {
      writer.append(value);
    }
  }

  @Override
  public String toString() {
    return this.value.toString();
  }
}
