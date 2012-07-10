package org.crsh.text;

import java.io.Serializable;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public enum Decoration implements Serializable {

  bold(1),
  underline(4),
  blink(5);

  /** . */
  public final int code;

  /** . */
  public final Style style;

  private Decoration(int code) {
    this.code = code;
    this.style = Style.style(this);
  }
}
