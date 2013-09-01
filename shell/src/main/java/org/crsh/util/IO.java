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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IO {

  public static byte[] readAsBytes(InputStream in) throws IOException {
    return read(in).toByteArray();
  }

  public static String readAsUTF8(InputStream in) {
    try {
      ByteArrayOutputStream baos = read(in);
      return baos.toString("UTF-8");
    }
    catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static void copy(InputStream in, OutputStream out) throws IOException {
    if (in == null) {
      throw new NullPointerException();
    }
    try {
      byte[] buffer = new byte[256];
      for (int l = in.read(buffer); l != -1; l = in.read(buffer)) {
        out.write(buffer, 0, l);
      }
    }
    finally {
      Safe.close(in);
    }
  }

  private static ByteArrayOutputStream read(InputStream in) throws IOException {
    if (in == null) {
      throw new NullPointerException();
    }
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    copy(in, baos);
    return baos;
  }
}
