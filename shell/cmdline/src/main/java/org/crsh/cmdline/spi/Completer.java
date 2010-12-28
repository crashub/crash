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

import java.util.Map;

/**
 * A completer provides completion suffixes for a given prefix. The cmdline framework uses it when
 * computing a completion.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public interface Completer {

  /**
   * <p>Query the completer for a set of completion for the given prefix. The returned Map
   * should provide the possible suffixes matching the prefix argument. Each map entry maps
   * to a possible completion: an entry key is the possible completion, its corresponding
   * boolean value indicates wether the value can be further more completed or not.
   *
   * The following guidelines
   * should be respected:
   *
   * <ul>
   * <li>An empty map means no completion can be determined, the framework will not do anything.</li>
   * <li>A singleton map means the match was entire and the framework will complete it with the sole map entry.</li>
   * <li>A map containing string sharing a common prefix instruct the framework to insert this common prefix.</li>
   * <li>A list containing strings with no common prefix (other than the empty string) instruct the framework to display
   * the list of possible completions.</li>
   * </ul>
   * <ul>When a match is considered as full (the entry value is set to true), the completion should contain a trailing value
   * that is usually a white space (but it could be a quote for quoted values).</ul>
   * </p>
   *
   * <p>Example: a completer that would complete colors could
   * <ul>
   * <li>return the map ["lack ":true,"lue ":true] for the prefix "b".</li>
   * <li>return the amp ["e ":true] for the prefix "blu".</li>
   * <li>return the map [] for the prefix "z".</li>
   * </ul>
   * </p>
   *
   * <p>Example: a completer that would complete java packages could
   * <ul>
   * <li>return the map ["ext":true,"ext.spi":false] for the prefix "java.t"</li>
   * </ul>
   * </p>
   *
   * @param parameter the completed parameter
   * @param prefix the prefix to complete
   * @return the possible suffix map
   * @throws Exception any exception that would prevent completion to perform correctly
   */
  Map<String, Boolean> complete(ParameterDescriptor<?> parameter, String prefix) throws Exception;

}
