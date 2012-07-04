package org.crsh.term;

import org.crsh.shell.ui.Color;
import org.crsh.shell.ui.Decoration;
import org.crsh.shell.ui.Style;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class ANSIFontBuilder {

  public CharSequence build(FormattingData formatting) {
    
    if (formatting.getStyle() != null) {

      Style style = formatting.getStyle();
      int decoration = buildStyle(style.getDecoration());
      int fg = buildColor(style.getForeground(), 30);
      int bg = buildColor(style.getBackground(), 40);

      if (decoration >= 0 || fg >= 0 || bg >= 0) {
        StringBuilder sb = new StringBuilder();

        if (decoration >= 0) {
          sb.append(String.valueOf(decoration));
        }

        if (fg >= 0) {
          if (sb.length() > 0) {
            sb.append(";");
          }
          sb.append(String.valueOf(fg));
        }

        if (bg >= 0) {
          if (sb.length() > 0) {
            sb.append(";");
          }
          sb.append(String.valueOf(bg));
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

  private int buildStyle(Decoration decoration) {

    if (decoration == null) {
      return -1;
    }

    switch (decoration) {

      case bold: return 1;
      case underline: return 4;
      case blink: return 5;

      default: return -1;

    }

  }

  private int buildColor(Color color, int base) {

    int colorCode = buildColor(color);
    if (colorCode >= 0) {
      return base + colorCode;
    }
    else {
      return -1;
    }

  }

  private int buildColor(Color color) {

    if (color == null) {
      return -1;
    }

    switch (color) {

      case black: return 0;
      case red: return 1;
      case green: return 2;
      case yellow: return 3;
      case blue: return 4;
      case magenta: return 5;
      case cyan: return 6;
      case white: return 7;

      default: return -1;
      
    }

  }

}
