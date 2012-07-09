package org.crsh.text;

import java.io.Serializable;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public enum Color implements Serializable {

  black(0),
  red(1),
  green(2),
  yellow(3),
  blue(4),
  magenta(5),
  cyan(6),
  white(7);

  /** . */
  public final int code;

  /** . */
  public final Style style;

  private Color(int code) {
    this.code = code;
    this.style = Style.style(this);
  }

  public int code(int base) {
    return base + code;
  }
}
