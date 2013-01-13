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
package org.crsh.command;

import org.crsh.io.Filter;

/**
 * A command invoker.
 *
 * @param <C> the consumed generic type
 * @param <P> the produced generic type
 */
public interface CommandInvoker<C, P> extends Filter<C, P> {

  /**
   * Associate the command invoker with a session, the association should be done before
   * the command is invoked.
   *
   * @param session the session
   */
  void setSession(CommandContext session);

}
