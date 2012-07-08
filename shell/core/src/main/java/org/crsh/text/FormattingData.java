package org.crsh.text;

import org.crsh.shell.ui.Decoration;

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

  @Override
  public String toString() {
    return "";
  }

  public CharSequence asAnsiSequence() {
    if (style != null) {
      Color fg = style.getForeground();
      Color bg = style.getBackground();
      Decoration decoration = style.getDecoration();
      if (decoration != null|| fg != null || bg != null) {
        StringBuilder sb = new StringBuilder();
        if (decoration != null) {
          sb.append(decoration.code);
        }
        if (fg != null) {
          if (sb.length() > 0) {
            sb.append(";");
          }
          sb.append(fg.code(30));
        }
        if (bg != null) {
          if (sb.length() > 0) {
            sb.append(";");
          }
          sb.append(bg.code(40));
        }
        sb.insert(0, "\033[");
        sb.append("m");
        return sb.toString();
      }
      else {
        return "";
      }
    }
    else {
      return "\033[0m";
    }
  }
}
