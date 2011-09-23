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

package org.crsh.shell.impl;

import org.crsh.command.CommandContext;

import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class CommandContextImpl implements CommandContext {

  /** . */
  private final Map<String, Object> attributes;

  CommandContextImpl(Map<String, Object> attributes) {
    this.attributes = attributes(attributes);
  }

  /**
   * Provide an opportunity to subclass to change the attribute map. This implementation returns the same
   * object by default.
   *
   * @param attributes the original attributes
   * @return the replaced attributes
   */
  protected Map<String, Object> attributes(Map<String, Object> attributes) {
    return attributes;
  }

  public final Map<String, Object> getAttributes() {
    return attributes;
  }
}
