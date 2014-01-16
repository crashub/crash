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
package org.crsh.repl;

import org.crsh.cli.impl.completion.CompletionMatch;

/**
 * Read–eval–print loop.
 *
 * @author Julien Viet
 */
public interface REPL {

  /**
   * Return true if this REPL is active. Implementation can decide based on the runtime, for instance the
   * Groovy REPL can be inactive when Groovy is not available at runtime.
   *
   * @return the active status
   */
  boolean isActive();

  /**
   * Return the repl name.
   *
   * @return the repl name
   */
  String getName();

  /**
   * Returns a descripton of the REPL.
   *
   * @return the repl description
   */
  String getDescription();

  /**
   * Evaluate a request
   *
   * @param session the session
   * @param request the request to evaluate
   * @return the evaluation response
   */
  EvalResponse eval(REPLSession session, String request);

  /**
   * Perform completion.
   *
   * @param session the session
   * @param prefix the prefix to complete
   * @return the completion match
   */
  CompletionMatch complete(REPLSession session, String prefix);

}
