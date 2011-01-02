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

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class PropertyInfo<T> {

  /** . */
  public static final PropertyInfo<Integer> SSH_PORT = new PropertyInfo<Integer>(Integer.class, "ssh.port", 2000, "The SSH port");

  /** . */
  public static final PropertyInfo<String> SSH_KEYPATH = new PropertyInfo<String>(String.class, "ssh.keypath", null, "The path to the key file");

  /** . */
  public static final PropertyInfo<Integer> TELNET_PORT = new PropertyInfo<Integer>(Integer.class, "telnet.port", 5000, "The telnet port");

  /** . */
  public final Class<T> type;

  /** . */
  public final String key;

  /** . */
  public final T defaultValue;

  /** . */
  public final String description;

  private PropertyInfo(Class<T> type, String key, T defaultValue) {
    this(type, key, defaultValue, null);
  }

  private PropertyInfo(Class<T> type, String key, T defaultValue, String description) {
    if (key == null) {
      throw new AssertionError();
    }
    this.type = type;
    this.key = key;
    this.defaultValue = defaultValue;
    this.description = description;
  }
}
