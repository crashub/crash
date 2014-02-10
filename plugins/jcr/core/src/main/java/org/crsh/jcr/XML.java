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
package org.crsh.jcr;

public class XML {

  public static String fileName(String qName) {
    int pos = qName.indexOf(':');
    if (pos == -1) {
      return qName;
    } else {
      return qName.substring(0, pos) + "__" + qName.substring(pos + 1);
    }
  }

  public static String qName(String fileName) {
    int pos = fileName.indexOf("__");
    if (pos == -1) {
      return fileName;
    } else {
      return fileName.substring(0, pos) + ":" + fileName.substring(pos + 2);
    }
  }

  public static String getPrefix(String qName) {
    int pos = qName.indexOf(':');
    return pos == -1 ? "" : qName.substring(0, pos);
  }
}
