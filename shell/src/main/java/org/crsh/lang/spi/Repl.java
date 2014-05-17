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

import org.crsh.cli.impl.completion.CompletionMatch;
import org.crsh.shell.impl.command.ShellSession;

/**
 * Read–eval–print loop.
 *
 * @author Julien Viet
 */
public interface Repl {

  /**
   * @return the language this repl belongs to
   */
  Language getLanguage();

  /**
   * @return a descripton of the Repl
   */
  String getDescription();

  /**
   * Evaluate a request.
   *
   * @param session the session
   * @param request the request to evaluate
   * @return the evaluation response
   */
  ReplResponse eval(ShellSession session, String request);

  /**
   * Perform completion.
   *
   * @param session the session
   * @param prefix the prefix to complete
   * @return the completion match
   */
  CompletionMatch complete(ShellSession session, String prefix);

}
