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

package org.crsh.auth;

import org.crsh.plugin.PropertyDescriptor;

import java.util.Collections;
import java.util.List;

/**
 * The authentication plugin.
 *
 * @param <C> the credential parameter type
 */
public interface AuthenticationPlugin<C> {

  /** The authentication plugin to use. */
  PropertyDescriptor<List> AUTH = PropertyDescriptor.create("auth", Collections.emptyList(), "The authentication plugin");

  /**
   * The plugin that never authenticates, returns the name value <code>null</code>.
   */
  AuthenticationPlugin<Object> NULL = new AuthenticationPlugin<Object>() {
    public Class<Object> getCredentialType() {
      return Object.class;
    }
    public String getName() {
      return "null";
    }
    public AuthInfo authenticate(String username, Object password) throws Exception {
      return AuthInfo.UNSUCCESSFUL;
    }
  };

  /**
   * Returns the authentication plugin name.
   *
   * @return the plugin name
   */
  String getName();

  /**
   * Returns the credential type.
   *
   * @return the credential type
   */
  Class<C> getCredentialType();

  /**
   * Returns true if the user is authentified by its username and credential.
   *
   * @param username the username
   * @param credential the credential
   * @return AuthInfo object
   * @throws Exception any exception that would prevent authentication to happen
   */
  AuthInfo authenticate(String username, C credential) throws Exception;
}
