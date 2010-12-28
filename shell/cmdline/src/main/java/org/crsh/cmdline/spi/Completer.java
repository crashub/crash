/*
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.crsh.cmdline.spi;

import org.crsh.cmdline.ParameterDescriptor;

import java.util.List;

/**
 * A completer provides completion suffixes for a given prefix. The cmdline framework uses it when
 * computing a completion.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public interface Completer {

  /**
   * <p>Query the completer for a set of completion for the given prefix. The returned list
   * should provide the possible suffixes matching the prefix argument. The following guidelines
   * should be respected:
   *
   * <ul>
   * <li>An empty list means no completion can be determined, the framework will not do anything.</li>
   * <li>A singleton list means the match was entire and the framework will complete it.</li>
   * <li>A list containing string with a common prefix should insert this common prefix by the framework.</li>
   * <li>A list containing strings with no common prefix other than the empty string should display the list
   * of possible completions. The shown result could be truncanted.</li>
   * </ul>
   * <ul>When a match is considered as full, the completion should contain a trailing white space.</ul>
   * </p>
   *
   * <p>Example, a completer that would complete path could
   * <ul>
   * <li>return the list ["lack ","lue "] for the prefix "b".</li>
   * <li>return the list ["e "] for the prefix "blu".</li>
   * <li>return the list [] for the prefix "z".</li>
   * </ul>
   * </p>
   *
   * @param parameter the completed parameter
   * @param prefix the prefix to complete
   * @return the possible suffixes
   * @throws Exception any exception that would prevent completion to perform correctly
   */
  List<String> complete(ParameterDescriptor<?> parameter, String prefix) throws Exception;

}
