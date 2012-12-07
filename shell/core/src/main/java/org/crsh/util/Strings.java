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

package org.crsh.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Strings {

  /** . */
  private static final Pattern p = Pattern.compile("\\S+");

  public static List<String> chunks(CharSequence s) {
    List<String> chunks = new ArrayList<String>();
    Matcher m = p.matcher(s);
    while (m.find()) {
      chunks.add(m.group());
    }
    return chunks;
  }

  public static String join(Iterable<String> strings, String separator) {
    Iterator<String> i = strings.iterator();
    if (i.hasNext()) {
      String first = i.next();
      if (i.hasNext()) {
        StringBuilder buf = new StringBuilder();
        buf.append(first);
        while (i.hasNext()) {
          buf.append(separator);
          buf.append(i.next());
        }
        return buf.toString();
      } else {
        return first;
      }
    } else {
      return "";
    }
  }

  public static String[] split(CharSequence s, char separator) {
    return foo(s, separator, 0, 0, 0);
  }

  public static String[] split(CharSequence s, char separator, int rightPadding) {
    if (rightPadding < 0) {
      throw new IllegalArgumentException("Right padding cannot be negative");
    }
    return foo(s, separator, 0, 0, rightPadding);
  }

  private static String[] foo(CharSequence s, char separator, int count, int from, int rightPadding) {
    int len = s.length();
    if (from < len) {
      int to = from;
      while (to < len && s.charAt(to) != separator) {
        to++;
      }
      String[] ret;
      if (to == len - 1) {
        ret = new String[count + 2 + rightPadding];
        ret[count + 1] = "";
      }
      else {
        ret = to == len ? new String[count + 1 + rightPadding] : foo(s, separator, count + 1, to + 1, rightPadding);
      }
      ret[count] = from == to ? "" : s.subSequence(from, to).toString();
      return ret;
    }
    else if (from == len) {
      return new String[count + rightPadding];
    }
    else {
      throw new AssertionError();
    }
  }

  /**
   * @see #findLongestCommonPrefix(Iterable)
   */
  public static String findLongestCommonPrefix(CharSequence... seqs) {
    return findLongestCommonPrefix(Arrays.asList(seqs));
  }

  /**
   * Find the longest possible common prefix of the provided char sequence.
   *
   * @param seqs the sequences
   * @return the longest possible prefix
   */
  public static String findLongestCommonPrefix(Iterable<? extends CharSequence> seqs) {
    String common = "";
    out:
    while (true) {
      String candidate = null;
      for (CharSequence s : seqs) {
        if (common.length() + 1 > s.length()) {
          break out;
        } else {
          if (candidate == null) {
            candidate = s.subSequence(0, common.length() + 1).toString();
          } else if (s.subSequence(0, common.length() + 1).toString().equals(candidate)) {
            // Ok it is a prefix
          } else {
            break out;
          }
        }
      }
      if (candidate == null) {
        break;
      } else {
        common = candidate;
      }
    }
    return common;
  }
}
