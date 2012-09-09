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

import org.crsh.util.Safe;
import org.crsh.util.Utils;

import java.io.IOException;
import java.io.Serializable;

public abstract class Style extends Chunk implements Serializable {

  public static final Style reset = new Style(null, null, null) {
    @Override
    public void writeAnsiTo(Appendable appendable) throws IOException {
      appendable.append("\033[0m");
    }
  };

  static class Regular extends Style {
    Regular(Decoration decoration, Color foreground, Color background) {
      super(decoration, foreground, background);
    }

    @Override
    public void writeAnsiTo(Appendable appendable) throws IOException {
      if (decoration != null|| foreground != null || background != null) {
        appendable.append("\033[");
        boolean appended = false;
        if (decoration != null) {
          appendable.append(Integer.toString(decoration.code));
          appended = true;
        }
        if (foreground != null) {
          if (appended) {
            appendable.append(";");
          }
          appendable.append(Integer.toString(foreground.code(30)));
          appended = true;
        }
        if (background != null) {
          if (appended) {
            appendable.append(";");
          }
          appendable.append(Integer.toString(background.code(40)));
        }
        appendable.append("m");
      }
      else {
        //
      }
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }
      if (obj instanceof Regular) {
        Regular that = (Regular)obj;
        return Safe.equals(decoration, that.decoration) &&
            Safe.equals(foreground, that.foreground) &&
            Safe.equals(background, that.background);
      }
      return false;
    }
  }

  public static Style style(Color foreground) {
    return new Regular(null, foreground, null);
  }

  public static Style style(Color foreground, Color background) {
    return new Regular(null, foreground, background);
  }

  public static Style style(Decoration decoration, Color foreground, Color background) {
    return new Regular(decoration, foreground, background);
  }

  public static Style style(Decoration decoration) {
    return new Regular(decoration, null, null);
  }

  public static Style style(Decoration decoration, Color foreground) {
    return new Regular(decoration, foreground, null);
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

  public Style merge(Style s) throws NullPointerException {
    if (s == null) {
      throw new NullPointerException();
    }
    if (s == reset) {
      return reset;
    } else {
      Decoration dec = Utils.notNull(s.decoration, decoration);
      Color fg = Utils.notNull(s.foreground, foreground);
      Color bg = Utils.notNull(s.background, background);
      return new Regular(dec, fg, bg);
    }
  }

  public CharSequence toAnsiSequence() {
    StringBuilder sb = new StringBuilder();
    try {
      writeAnsiTo(sb);
    }
    catch (IOException e) {
      // Should not happen
      throw new AssertionError(e);
    }
    return sb.toString();
  }

  public abstract void writeAnsiTo(Appendable appendable) throws IOException;

  @Override
  public String toString() {
    return "Style[decoration=" + decoration + ",background=" + background + ",foreground=" + foreground + "]";
  }
}
