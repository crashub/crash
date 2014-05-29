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

package org.crsh.shell;

import org.crsh.text.ScreenContext;

import java.io.IOException;

/**
 * The interaction context extends the screen context and provides interaction with the client.
 */
public interface InteractionContext extends ScreenContext {

  /**
   * Take control of the alternate buffer. When the alternate buffer is already used
   * nothing happens. The buffer switch should occur when then {@link #flush()} method
   * is invoked.
   *
   * @return true if the alternate buffer is shown
   */
  boolean takeAlternateBuffer() throws IOException;

  /**
   * Release control of the alternate buffer. When the normal buffer is already used
   * nothing happens. The buffer switch should occur when then {@link #flush()} method
   * is invoked.
   *
   * @return true if the usual buffer is shown
   */
  boolean releaseAlternateBuffer() throws IOException;

  /**
   * Returns a generic property, usually this property is resolved by the
   * shell client.
   *
   * @param propertyName the property name
   * @return the property value
   */
  String getProperty(String propertyName);

  /**
   * Display a message and read a line on the console, this method call can be blocking until the user provides
   * a value. If no line can be read then null is returned.
   *
   * @param msg the message to display before reading a line
   * @param echo wether or not the line read should be echoed when typing
   * @return the line read
   * @throws java.io.IOException any io exception
   * @throws java.lang.InterruptedException the thread was interrupted while waiting for the user value
   * @throws java.lang.IllegalStateException if reading a line is not at the appropriate time
   */
  String readLine(String msg, boolean echo) throws IOException, InterruptedException, IllegalStateException;

}
