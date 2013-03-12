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

package org.crsh.cli.spi;

import org.crsh.cli.descriptor.ParameterDescriptor;

public interface Completer {

  /**
   * <p>Query the completer for a set of completions for the given prefix. The returned {@link Completion} object
   * provides the possible completion matching the prefix argument.<p>
   *
   * @param parameter the completed parameter
   * @param prefix the prefix to complete
   * @return the possible suffix map
   * @throws Exception any exception that would prevent completion to perform correctly
   */
  Completion complete(ParameterDescriptor parameter, String prefix) throws Exception;

}
