package org.crsh.term;

import org.crsh.shell.ui.Style;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class FormattingData extends DataFragment {

  /** . */
  private Style style;

  public FormattingData(Style style) {
    super(null);
    this.style = style;
  }

  public Style getStyle() {
    return style;
  }
  
}
