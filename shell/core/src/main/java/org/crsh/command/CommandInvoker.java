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

/**
 * A command invoker.
 *
 * @param <C> the consumed generic type
 * @param <P> the produced generic type
 */
public interface CommandInvoker<C, P> {

  /**
   * Returns the class of the produced type.
   *
   * @return the produced type
   */
  Class<P> getProducedType();

  /**
   * Returns the class of the consumed type.
   *
   * @return the consumed type
   */
  Class<C> getConsumedType();

  /**
   * Invoke a command
   *
   * @param context the command execution context
   * @throws ScriptException any script exception
   * @return the related pipe
   */
  PipeCommand<C> invoke(InvocationContext<P> context) throws ScriptException;

}
