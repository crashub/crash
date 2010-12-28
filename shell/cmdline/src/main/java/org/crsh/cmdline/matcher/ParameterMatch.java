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

import org.crsh.cmdline.Delimiter;
import org.crsh.cmdline.binding.TypeBinding;
import org.crsh.cmdline.ParameterDescriptor;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ParameterMatch<P extends ParameterDescriptor<B>, B extends TypeBinding> {

  /** . */
  private final P parameter;

  /** . */
  private final List<String> rawValues;

  /** . */
  private final List<String> values;

  /** . */
  private final List<Delimiter> delimiters;

  public ParameterMatch(P parameter, List<String> rawValues) {

    // Unquote if necessary
    ArrayList<String> values = new ArrayList<String>(rawValues);
    ArrayList<Delimiter> delimiters = new ArrayList<Delimiter>();
    ListIterator<String> i = values.listIterator();
    while (i.hasNext()) {
      String s = i.next();
      Delimiter delimiter = Delimiter.WHITE_SPACE;
      if (s != null) {
        if (s.length() == 1) {
          char c = s.charAt(0);
          switch (c) {
            case '\'':
              i.set("");
              delimiter = Delimiter.SIMPLE_QUOTE;
              break;
            case '"':
              i.set("");
              delimiter = Delimiter.DOUBLE_QUOTE;
              break;
          }
        } else if (s.length() >= 2) {
          char first = s.charAt(0);
          char last = s.charAt(s.length() - 1);
          if (first == '"') {
            delimiter = Delimiter.DOUBLE_QUOTE;
            if (last == '"') {
              i.set(s.substring(1, s.length() - 1));
            } else {
              i.set(s.substring(1));
            }
          } else if (first == '\'') {
            delimiter = Delimiter.SIMPLE_QUOTE;
            if (last == '\'') {
              i.set(s.substring(1, s.length() - 1));
            } else {
              i.set(s.substring(1));
            }
          }
        }
      }
      delimiters.add(delimiter);
    }

    //
    this.parameter = parameter;
    this.rawValues = rawValues;
    this.values = values;
    this.delimiters = delimiters;
  }

  public P getParameter() {
    return parameter;
  }

  public List<String> getRawValues() {
    return rawValues;
  }

  public List<String> getValues() {
    return values;
  }

  public List<Delimiter> getDelimiters() {
    return delimiters;
  }
}
