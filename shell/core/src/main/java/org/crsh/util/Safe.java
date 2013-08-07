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

import javax.naming.Context;
import javax.naming.NamingException;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Safe {

  /**
   * Close the socket and catch any exception thrown.
   *
   * @param socket the socket to close
   * @return any Exception thrown during the <code>close</code> operation
   */
  public static Exception close(Socket socket) {
    if (socket != null) {
      try {
        socket.close();
      }
      catch (Exception e) {
        return e;
      }
    }
    return null;
  }

  /**
   * Close the closeable and catch any exception thrown.
   *
   * @param closeable the closeable to close
   * @return any Exception thrown during the <code>close</code> operation
   */
  public static Exception close(Closeable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      }
      catch (Exception e) {
        return e;
      }
    }
    return null;
  }

  /**
   * Close the connection and catch any exception thrown.
   *
   * @param connection the socket to close
   * @return any Exception thrown during the <code>close</code> operation
   */
  public static Exception close(Connection connection) {
    if (connection != null) {
      try {
        connection.close();
      }
      catch (Exception e) {
        return e;
      }
    }
    return null;
  }

  /**
   * Close the statement and catch any exception thrown.
   *
   * @param statement the statement to close
   * @return any Exception thrown during the <code>close</code> operation
   */
  public static Exception close(Statement statement) {
    if (statement != null) {
      try {
        statement.close();
      }
      catch (Exception e) {
        return e;
      }
    }
    return null;
  }

  /**
   * Close the result set and catch any exception thrown.
   *
   * @param rs the result set to close
   * @return any Exception thrown during the <code>close</code> operation
   */
  public static Exception close(ResultSet rs) {
    if (rs != null) {
      try {
        rs.close();
      }
      catch (Exception e) {
        return e;
      }
    }
    return null;
  }

  /**
   * Close the context and catch any exception thrown.
   *
   * @param context the context to close
   * @return any Exception thrown during the <code>close</code> operation
   */
   public static Exception close(Context context) {
      if (context != null) {
         try {
            context.close();
         }
         catch (Exception e) {
           return e;
         }
      }
     return null;
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

  public static boolean equals(Object o1, Object o2) {
    return o1 == null ? o2 == null : (o2 != null && o1.equals(o2));
  }

  public static boolean notEquals(Object o1, Object o2) {
    return !equals(o1, o2);
  }

  /**
   * Flush the flushable and catch any exception thrown.
   *
   * @param flushable the flushable to flush
   * @return any Exception thrown during the <code>flush</code> operation
   */
  public static Exception flush(Flushable flushable) {
    if (flushable != null) {
      try {
        flushable.flush();
      }
      catch (Exception e) {
        return e;
      }
    }
    return null;
  }
}
