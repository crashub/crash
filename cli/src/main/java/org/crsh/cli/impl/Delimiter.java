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

package org.crsh.cli.impl;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;

public enum Delimiter {

  EMPTY(' ') {
    @Override
    public void escape(CharSequence s, int start, int end, Appendable appendable) throws IOException {
      while (start < end) {
        char c = s.charAt(start++);
        switch (c) {
          case ' ':
          case '"':
          case '\'':
          case '\\':
            appendable.append('\\');
        }
        appendable.append(c);
      }
    }
  },

  SINGLE_QUOTE('\'') {
    @Override
    public void escape(CharSequence s, int start, int end, Appendable appendable) throws IOException {
      while (start < end) {
        while (start < end) {
          char c = s.charAt(start++);
          switch (c) {
            case '\'':
            case '\\':
              appendable.append('\\');
          }
          appendable.append(c);
        }
      }
    }
  },

  DOUBLE_QUOTE('"') {
    @Override
    public void escape(CharSequence s, int start, int end, Appendable appendable) throws IOException {
      while (start < end) {
        while (start < end) {
          char c = s.charAt(start++);
          switch (c) {
            case '"':
            case '\\':
              appendable.append('\\');
          }
          appendable.append(c);
        }
      }
    }
  };

  /** . */
  private final char value;

  Delimiter(char value) {
    this.value = value;
  }

  public char getValue() {
    return value;
  }

  public final String escape(CharSequence s) {
    try {
      StringBuilder buffer = new StringBuilder();
      escape(s, buffer);
      return buffer.toString();
    }
    catch (IOException e) {
      throw new UndeclaredThrowableException(e);
    }
  }

  public final void escape(CharSequence s, Appendable appendable) throws IOException {
    escape(s, 0, s.length(), appendable);
  }

  public abstract void escape(CharSequence s, int start, int end, Appendable appendable) throws IOException;
}
