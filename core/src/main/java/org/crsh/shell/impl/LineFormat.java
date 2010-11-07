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
package org.crsh.shell.impl;

import org.crsh.command.ScriptException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class LineFormat {

  public static List<List<String>> parse(String s) throws ScriptException {
    List<List<String>> atoms = new ArrayList<List<String>>();
    int from = 0;
    while (true) {
      int to = s.indexOf('|', from);
      if (to == - 1) {
        break;
      }
      atoms.add(parse2(s.substring(from, to)));
      from = to + 1;
    }
    atoms.add(parse2(s.substring(from)));
    return atoms;
  }

  private static List<String> parse2(String s) throws ScriptException {
    List<String> atoms = new ArrayList<String>();
    int from = 0;
    while (true) {
      int to = s.indexOf('+', from);
      if (to == - 1) {
        break;
      }
      atoms.add(s.substring(from, to));
      from = to + 1;
    }
    atoms.add(s.substring(from));
    return atoms;
  }

  /**
   * Format a line
   * @param s
   * @return
   * @throws NullPointerException if the argument is null
   * @throws ScriptException
   */
  public static List<String> format(String s) throws NullPointerException, ScriptException {
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
            chunk.append(c);
          } else if (lastQuote != c) {
            chunk.append(c);
          } else {
            chunk.append(c);
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
