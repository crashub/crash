/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.crsh.text;

import org.crsh.util.Utils;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;

/**
 * A control for the text stylistric attributes:
 * <u>
 *   <li>background color</li>
 *   <li>foreground color</li>
 *   <li>underline</li>
 *   <li>bold</li>
 *   <li>blink</li>
 * </u>
 *
 * A style is either a composite style or the {@link #reset} style. Styles can be composed together to form a new
 * style <code>style.merge(other)</code>.
 */
public abstract class Style implements Serializable {

  public static final Style reset = new Style() {

    @Override
    public Style merge(Style s) throws NullPointerException {
      if (s == null) {
        throw new NullPointerException();
      }
      return s;
    }

    @Override
    public String toString() {
      return "Style.Reset[]";
    }

    @Override
    public void writeAnsiTo(Appendable appendable) throws IOException {
      appendable.append("\033[0m");
    }
  };

  public static final class Composite extends Style {

    /** . */
    protected final Boolean bold;

    /** . */
    protected final Boolean underline;

    /** . */
    protected final Boolean blink;

    /** . */
    protected final Color foreground;

    /** . */
    protected final Color background;

    private Composite(Boolean bold, Boolean underline, Boolean blink, Color foreground, Color background) {
      this.bold = bold;
      this.underline = underline;
      this.blink = blink;
      this.foreground = foreground;
      this.background = background;
    }

    public Composite fg(Color color) {
      return foreground(color);
    }

    public Composite foreground(Color color) {
      return style(bold, underline, blink, color, background);
    }

    public Composite bg(Color value) {
      return background(value);
    }

    public Composite background(Color value) {
      return style(bold, underline, blink, foreground, value);
    }

    public Composite bold() {
      return bold(true);
    }

    public Composite underline() {
      return underline(true);
    }

    public Composite blink() {
      return blink(true);
    }

    public Composite bold(Boolean value) {
      return style(value, underline, blink, foreground, background);
    }

    public Composite underline(Boolean value) {
      return style(bold, value, blink, foreground, background);
    }

    public Composite blink(Boolean value) {
      return style(bold, underline, value, foreground, background);
    }

    public Composite decoration(Decoration decoration) {
      if (decoration != null) {
        switch (decoration) {
          case bold:
            return bold(true);
          case bold_off:
            return bold(false);
          case underline:
            return underline(true);
          case underline_off:
            return underline(false);
          case blink:
            return blink(true);
          case blink_off:
            return blink(false);
        }
      }
      return this;
    }

    public Boolean getBold() {
      return bold;
    }

    public Boolean getUnderline() {
      return underline;
    }

    public Boolean getBlink() {
      return blink;
    }

    public Color getForeground() {
      return foreground;
    }

    public Color getBackground() {
      return background;
    }

    public Style merge(Style s) throws NullPointerException {
      if (s == null) {
        throw new NullPointerException();
      }
      if (s == reset) {
        return reset;
      } else {
        Style.Composite that = (Composite)s;
        Boolean bold = Utils.notNull(that.getBold(), getBold());
        Boolean underline = Utils.notNull(that.getUnderline(), getUnderline());
        Boolean blink = Utils.notNull(that.getBlink(), getBlink());
        Color foreground = Utils.notNull(that.getForeground(), getForeground());
        Color background = Utils.notNull(that.getBackground(), getBackground());
        return style(bold, underline, blink, foreground, background);
      }
    }

    @Override
    public String toString() {
      return "Style.Composite[bold=" + bold + ",underline=" + underline + ",blink=" + blink +
          ",background=" + background + ",foreground=" + foreground + "]";
    }

    private static boolean decoration(
        Appendable appendable,
        String on,
        String off,
        Boolean value,
        boolean append) throws IOException {
      if (value != null) {
        if (append) {
          appendable.append(';');
        } else {
          appendable.append("\033[");
        }
        if (value) {
          appendable.append(on);
        } else {
          appendable.append(off);
        }
        return true;
      }
      return false;
    }

    private static boolean color(
        Appendable appendable,
        Color color,
        char base,
        boolean append) throws IOException {
      if (color != null) {
        if (append) {
          appendable.append(';');
        } else {
          appendable.append("\033[");
        }
        appendable.append(base);
        appendable.append(color.c);
        return true;
      }
      return false;
    }

    @Override
    public void writeAnsiTo(Appendable appendable) throws IOException {
      boolean appended = decoration(appendable, Decoration.bold.code, Decoration.bold_off.code, bold, false);
      appended |= decoration(appendable, Decoration.underline.code, Decoration.underline_off.code, underline, appended);
      appended |= decoration(appendable, Decoration.blink.code, Decoration.blink_off.code, blink, appended);
      appended |= color(appendable, foreground, '3', appended);
      appended |= color(appendable, background, '4', appended);
      if (appended) {
        appendable.append("m");
      }
    }
  }

  /** . */
  private static final Boolean[] BOOLEANS = {true,false,null};

  /** . */
  private static final Color[] COLORS = Arrays.copyOf(Color.values(), Color.values().length + 1);

  /** [bold][underline][blink][foreground][background]. */
  private static final Composite[][][][][] ALL;

  static {
    ALL = new Composite[BOOLEANS.length][][][][];
    for (int bold = 0;bold < BOOLEANS.length;bold++) {
      ALL[bold] = new Composite[BOOLEANS.length][][][];
      for (int underline = 0;underline < BOOLEANS.length;underline++) {
        ALL[bold][underline] = new Composite[BOOLEANS.length][][];
        for (int blink = 0;blink < BOOLEANS.length;blink++) {
          ALL[bold][underline][blink] = new Composite[COLORS.length][];
          for (int foreground = 0;foreground < COLORS.length;foreground++) {
            ALL[bold][underline][blink][foreground] = new Composite[COLORS.length];
            for (int background = 0;background < COLORS.length;background++) {
              ALL[bold][underline][blink][foreground][background] = new Composite(
                  BOOLEANS[bold],
                  BOOLEANS[underline],
                  BOOLEANS[blink],
                  COLORS[foreground],
                  COLORS[background]);
            }
          }
        }
      }
    }
  }

  public static Composite style(Color foreground) {
    return style(null, foreground, null);
  }

  public static Composite style(Color foreground, Color background) {
    return style(null, foreground, background);
  }

  public static Composite style(Decoration decoration, Color foreground, Color background) {
    Boolean bold = null;
    Boolean underline = null;
    Boolean blink = null;
    if (decoration != null) {
      switch (decoration) {
        case bold:
          bold = true;
          break;
        case bold_off:
          bold = false;
          break;
        case underline:
          underline = true;
          break;
        case underline_off:
          underline = false;
          break;
        case blink:
          blink = true;
          break;
        case blink_off:
          blink = false;
          break;
      }
    }
    return style(bold, underline, blink, foreground, background);
  }

  public static Composite style(Boolean bold, Boolean underline, Boolean blink, Color foreground, Color background) {
    int bo = bold != null ? bold ? 0 : 1: 2;
    int un = underline != null ? underline ? 0 : 1: 2;
    int bl = blink != null ? blink ? 0 : 1: 2;
    int fg = foreground != null ? foreground.ordinal() : COLORS.length - 1;
    int bg = background != null ? background.ordinal() : COLORS.length - 1;
    return ALL[bo][un][bl][fg][bg];
  }

  /**
   * Create a new blank style.
   *
   * @return the style
   */
  public static Composite style() {
    return style(null, null, null);
  }

  public static Composite style(Decoration decoration) {
    return style(decoration, null, null);
  }

  public static Composite style(Decoration decoration, Color foreground) {
    return style(decoration, foreground, null);
  }

  public abstract Style merge(Style s) throws NullPointerException;

  public CharSequence toAnsiSequence() {
    StringBuilder sb = new StringBuilder();
    try {
      writeAnsiTo(sb);
    }
    catch (IOException e) {
      // Should not happen
      throw new UndeclaredThrowableException(e);
    }
    return sb.toString();
  }

  public abstract void writeAnsiTo(Appendable appendable) throws IOException;

  @Override
  public abstract String toString();
}
