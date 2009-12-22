/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.crsh.shell;

import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class LineFormat {

  public static List<String> format(String s) throws ScriptException {
    if (s == null) {
      throw new NullPointerException();
    }
    LinkedList<String> chunks = new LinkedList<String>();
    StringBuilder chunk = new StringBuilder();
    Character lastQuote = null;
    for (int i = 0;i < s.length();i++) {
      char c = s.charAt(i);
      switch (c) {
        case ' ':
          if (lastQuote == null) {
            if (chunk.length() > 0) {
              chunks.addLast(chunk.toString());
              chunk.setLength(0);
            }
          } else {
            chunk.append(c);
          }
          break;
        case '"':
        case '\'':
          if (lastQuote == null) {
            lastQuote = c;
          } else if (lastQuote != c) {
            chunk.append(c);
          } else {
            lastQuote = null;
          }
          break;
        default:
          chunk.append(c);
          break;
      }
    }
    if (lastQuote != null) {
      throw new ScriptException("Quote " + lastQuote + " is not closed");
    }
    if (chunk.length() > 0) {
      chunks.addLast(chunk.toString());
    }
    return chunks;
  }
}
