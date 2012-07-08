package org.crsh.text;

import org.crsh.shell.ui.Decoration;

import java.io.Serializable;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public abstract class Style implements Serializable {

  public static final Style RESET = new Style(null, null, null) {
    @Override
    public CharSequence asAnsiSequence() {
      return "\033[0m";
    }
  };

  public static Style create(Decoration decoration, Color foreground, Color background) {
    return new Style(decoration, foreground, background) {
      public CharSequence asAnsiSequence() {
        if (decoration != null|| foreground != null || background != null) {
          StringBuilder sb = new StringBuilder();
          if (decoration != null) {
            sb.append(decoration.code);
          }
          if (foreground != null) {
            if (sb.length() > 0) {
              sb.append(";");
            }
            sb.append(foreground.code(30));
          }
          if (background != null) {
            if (sb.length() > 0) {
              sb.append(";");
            }
            sb.append(background.code(40));
          }
          sb.insert(0, "\033[");
          sb.append("m");
          return sb.toString();
        }
        else {
          return "";
        }
      }
    };
  }

  /** . */
  protected final Decoration decoration;

  /** . */
  protected final Color foreground;

  /** . */
  protected final Color background;

  private Style(Decoration decoration, Color foreground, Color background) {

    this.decoration = decoration;
    this.foreground = foreground;
    this.background = background;

  }

  public Decoration getDecoration() {
    return decoration;
  }

  public Color getForeground() {
    return foreground;
  }

  public Color getBackground() {
    return background;
  }

  public abstract CharSequence asAnsiSequence();}
