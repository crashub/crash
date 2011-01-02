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

package org.crsh.config;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Config {

  /** . */
  private final Map<String, ConfigProperty<?>> properties;

  public Config() {
    this.properties = new HashMap<String, ConfigProperty<?>>();
  }

  public <T> void setProperty(PropertyInfo<T> key, T value) {
    if (key == null) {
      throw new NullPointerException();
    }

    //
    if (value != null) {
      properties.put(key.name, new ConfigProperty<T>(key, value));
    } else {
      properties.remove(key.name);
    }
  }

}
