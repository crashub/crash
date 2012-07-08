package org.crsh.text;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class FormattingData extends DataFragment {

  /** . */
  private Style style;

  public FormattingData(Style style) throws NullPointerException {
    super(null);

    //
    if (style == null) {
      throw new NullPointerException();
    }

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
    return style.asAnsiSequence();
  }
}
