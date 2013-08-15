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
package org.crsh.golo;

import org.crsh.command.InvocationContext;

import java.io.IOException;
import java.util.Map;

/**
 * Golo API for CRaSH, should be imported in a Golo module.
 *
 * @author Julien Viet
 */
public class CRaSH {

  /** . */
  static final ThreadLocal<Context> current = new ThreadLocal<>();

  /**
   * @return the current invocation context
   */
  public static InvocationContext<Object> context() {
    return current.get().commandContext;
  }

  /**
   * Provide an object to the pipe.
   *
   * @param o the object
   * @throws IOException any io exception
   */
  public static void provide(Object o) throws IOException {
    context().provide(o);
  }

  /**
   * Flush the pipe
   *
   * @throws IOException any io exception
   */
  public static void flush() throws IOException {
    context().flush();
  }

  /**
   * @return the current height
   */
  public static int height() {
    return context().getHeight();
  }

  /**
   * @return the current width
   */
  public static int width() {
    return context().getWidth();
  }

  /**
   * @return the current user session
   */
  public static Map<String, Object> session() {
    return context().getSession();
  }

  /**
   * @return the current attributes
   */
  public static Map<String, Object> attributes() {
    return context().getAttributes();
  }
}
