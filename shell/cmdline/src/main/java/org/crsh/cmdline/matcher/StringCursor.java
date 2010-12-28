/*
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.crsh.cmdline.matcher;

/**
* @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
* @version $Revision$
*/
final class StringCursor implements CharSequence {

  /** . */
  private int start;

  /** . */
  private int end;

  /** . */
  private final String sanitized;

  /** . */
  private final String original;

  StringCursor(String s) {

    // Build a sanitized version of the string
    Character lastQuote = null;
    StringBuilder sb = new StringBuilder();
    for (char c : s.toCharArray()) {
      if (lastQuote == null) {
        if (c == '\'' || c == '"') {
          lastQuote = c;
          c = ' ';
        }
        sb.append(c);
      } else {
        if (c == lastQuote) {
          c = ' ';
          lastQuote = null;
        } else if (Character.isWhitespace(c)) {
          c = '_';
        }
        sb.append(c);
      }
    }

    //
    this.sanitized = sb.toString();
    this.start = 0;
    this.end = s.length();
    this.original = s;
  }

  private StringCursor(StringCursor that, int start, int end) {
    this.sanitized = that.sanitized;
    this.start = start;
    this.end = end;
    this.original = that.original;
  }

  public String getOriginal() {
    return original.substring(start, end);
  }

  public int getStart() {
    return start;
  }

  void seek(int to) {
    skip(to - start);
  }

  void skip(int delta) {
    if (delta < 0) {
      throw new AssertionError();
    }
    start += delta;
  }

  boolean isEmpty() {
    return start == end;
  }

  public char charAt(int idx) {
    if (idx < 0 || idx > end - start) {
      throw new StringIndexOutOfBoundsException();
    }
    return sanitized.charAt(start + idx);
  }

  public CharSequence subSequence(int start, int end) {
    return new StringCursor(this, this.start + start, this.start + end);
  }

  public int length() {
    return end - start;
  }


  public String toString() {
    return sanitized.substring(start, end);
  }

  String group(java.util.regex.Matcher matcher, int index) {
    if (matcher.start(index) == -1 && matcher.end(index) == -1) {
      return null;
    } else {
      return original.substring(start + matcher.start(index), start + matcher.end(index));
    }
  }
}
