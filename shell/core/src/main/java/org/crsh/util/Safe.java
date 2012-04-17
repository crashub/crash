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
package org.crsh.util;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Safe {

  public static void close(Socket socket) {
    if (socket != null) {
      try {
        socket.close();
      }
      catch (IOException ignore) {
      }
    }
  }

  public static void close(Closeable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      }
      catch (IOException ignore) {
      }
    }
  }

  public static void close(Connection connection) {
    if (connection != null) {
      try {
        connection.close();
      }
      catch (SQLException ignore) {
      }
    }
  }

  public static void close(Statement statement) {
    if (statement != null) {
      try {
        statement.close();
      }
      catch (SQLException ignore) {
      }
    }
  }

  public static void close(ResultSet rs) {
    if (rs != null) {
      try {
        rs.close();
      }
      catch (SQLException ignore) {
      }
    }
  }

  public static <T extends Throwable> void rethrow(Class<T> throwableClass, Throwable cause) throws T {
    T throwable;

    //
    try {
      throwable = throwableClass.newInstance();
    }
    catch (Exception e) {
      throw new AssertionError(e);
    }

    //
    throwable.initCause(cause);

    //
    throw throwable;
  }
}
