package org.crsh.shell.ui;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class Style {

  /** . */
  private Decoration decoration;

  /** . */
  private Color foreground;

  /** . */
  private Color background;

  public Style(Decoration decoration, Color foreground, Color background) {

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

}
