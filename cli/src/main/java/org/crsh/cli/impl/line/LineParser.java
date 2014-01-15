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
package org.crsh.cli.impl.line;

/**
 * Line parser.
 *
 * @author Julien Viet
 */
public final class LineParser {

  public static abstract class Visitor {
    public void onChar(int index, Quoting quoting, boolean backslash, char c) { }
    public void openStrongQuote(int index) {}
    public void closeStrongQuote(int index) {}
    public void openWeakQuote(int index) {}
    public void closeWeakQuote(int index) {}
    public void reset() {}
  }

  /** . */
  private static final int NORMAL = 0, WEAK_QUOTING = 1, STRONG_QUOTING = 2;

  /** . */
  private int status = NORMAL;

  /** . */
  private boolean escaped = false;

  /** . */
  private int index = 0;

  /** . */
  private final Visitor[] visitors;

  public LineParser(Visitor... visitors) {
    this.visitors = visitors;
  }

  public boolean crlf() {
    if (escaped) {
      escaped = false;
      return false;
    } else {
      switch (status) {
        case WEAK_QUOTING:
          for (Visitor visitor : visitors) visitor.onChar(index, Quoting.WEAK, false, '\n');
          index++;
          return false;
        case STRONG_QUOTING:
          for (Visitor visitor : visitors) visitor.onChar(index, Quoting.STRONG, false, '\n');
          index++;
          return false;
        default:
          return true;
      }
    }
  }

  public LineParser append(CharSequence s) {
    int len = s.length();
    for (int index = 0;index < len;index++) {
      append(s.charAt(index));
    }
    return this;
  }

  public LineParser append(char c) {
    if (!escaped) {
      switch (status) {
        case NORMAL:
          switch (c) {
            case '\\':
              escaped = true;
              break;
            case '\"':
              for (Visitor visitor : visitors) visitor.openWeakQuote(index);
              status = WEAK_QUOTING;
              index++;
              break;
            case '\'':
              for (Visitor visitor : visitors) visitor.openStrongQuote(index);
              index++;
              status = STRONG_QUOTING;
              break;
            default:
              for (Visitor visitor : visitors) visitor.onChar(index, null, false, c);
              index++;
              break;
          }
          break;
        case WEAK_QUOTING:
          switch (c) {
            case '"':
              for (Visitor visitor : visitors) visitor.closeWeakQuote(index);
              index++;
              status = NORMAL;
              break;
            case '\\':
              escaped = true;
              break;
            default:
              for (Visitor visitor : visitors) visitor.onChar(index, Quoting.WEAK, false, c);
              index++;
              break;
          }
          break;
        case STRONG_QUOTING:
          switch (c) {
            case '\'':
              for (Visitor visitor : visitors) visitor.closeStrongQuote(index);
              index++;
              status = NORMAL;
              break;
            case '\\':
              escaped = true;
              break;
            default:
              for (Visitor visitor : visitors) visitor.onChar(index, Quoting.STRONG, false, c);
              index++;
              break;
          }
          break;
      }
    } else {
      switch (status) {
        case NORMAL:
          for (Visitor visitor : visitors) visitor.onChar(index + 1, null, true, c);
          index += 2;
          break;
        case WEAK_QUOTING:
          for (Visitor visitor : visitors) visitor.onChar(index + 1, Quoting.WEAK, true, c);
          index += 2;
          break;
        case STRONG_QUOTING:
          if (c == '\'') {
            // Special case
            status = NORMAL;
            for (Visitor visitor : visitors) visitor.onChar(index, Quoting.STRONG, false, '\\');
            for (Visitor visitor : visitors) visitor.closeStrongQuote(index + 1);
            index += 2;
          } else {
            for (Visitor visitor : visitors) visitor.onChar(index + 1, Quoting.STRONG, true, c);
            index += 2;
          }
          break;
      }
      escaped = false;
    }
    return this;
  }

  public void reset() {
    index = 0;
    status = NORMAL;
    escaped = false;
    for (Visitor visitor : visitors) visitor.reset();
  }
}
