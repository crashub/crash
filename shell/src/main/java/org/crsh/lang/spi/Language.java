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
package org.crsh.lang.spi;

import org.crsh.shell.impl.command.ShellSession;

/**
 * Integration of a language.
 *
 * @author Julien Viet
 */
public interface Language {

  /**
   * @return the language name
   */
  String getName();

  /**
   * @return the language display name, including the version if possible
   */
  String getDisplayName();

  /**
   * @return true if this language is active. Implementation can decide based on the runtime, for instance the
   *         Groovy language can be inactive when the Groovy runtime is not available
   */
  boolean isActive();

  /**
   * @return the repl for this language, null should be returned when the language does not support the repl feature
   */
  Repl getRepl();

  /**
   * @return the compiler for this language, null should be returned when the language does not support the compilation feature
   */
  Compiler getCompiler();

  /**
   * Init session callback.
   *
   * @param session the initialized session
   */
  void init(ShellSession session);

  /**
   * Destroy session callback.
   *
   * @param session the destroyed session
   */
  void destroy(ShellSession session);

}
