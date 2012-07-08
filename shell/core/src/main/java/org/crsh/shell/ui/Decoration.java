package org.crsh.shell.ui;

import java.io.Serializable;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public enum Decoration implements Serializable {

  bold(1),
  underline(4),
  blink(5);

  public final int code;

  private Decoration(int code) {
    this.code = code;
  }
}
