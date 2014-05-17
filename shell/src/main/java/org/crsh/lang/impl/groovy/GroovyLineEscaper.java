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
package org.crsh.lang.impl.groovy;

import org.crsh.cli.impl.line.LineParser;
import org.crsh.cli.impl.line.Quoting;

/**
 * Escape CRLF to '\\' + 'n' inside quotes.
 *
 * @author Julien Viet
 */
class GroovyLineEscaper extends LineParser.Visitor {

  /** . */
  final StringBuilder buffer = new StringBuilder();
  
  @Override
  public void onChar(int index, Quoting quoting, boolean backslash, char c) {
    if (quoting != null && c == '\n') {
      buffer.append('\\').append('n');
    } else {
      if (backslash) {
        buffer.append('\\');
      }
      buffer.append(c);
    }
  }

  @Override
  public void openStrongQuote(int index) {
    buffer.append("'");
  }

  @Override
  public void closeStrongQuote(int index) {
    buffer.append("'");
  }

  @Override
  public void openWeakQuote(int index) {
    buffer.append("\"");
  }

  @Override
  public void closeWeakQuote(int index) {
    buffer.append("\"");
  }
}
