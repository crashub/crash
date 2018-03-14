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

import org.crsh.plugin.CRaSHPlugin;
import org.crsh.plugin.PluginContext;
import org.crsh.plugin.PropertyDescriptor;

import java.util.Arrays;

public class SimpleAuthenticationPlugin extends
  CRaSHPlugin<AuthenticationPlugin> implements
  AuthenticationPlugin<String> {

  /** The username. */
  public static final PropertyDescriptor<String> SIMPLE_USERNAME =
    PropertyDescriptor.create(
      "auth.simple.username",
      "admin",
      "The username");

  /** The password. */
  public static final PropertyDescriptor<String> SIMPLE_PASSWORD =
    PropertyDescriptor.create(
      "auth.simple.password",
      "admin",
      "The password",
      true);

  /** . */
  private String username;

  /** . */
  private String password;

  @Override
  protected Iterable<PropertyDescriptor<?>> createConfigurationCapabilities() {
    return Arrays.<PropertyDescriptor<?>>asList(
      SIMPLE_USERNAME,
      SIMPLE_PASSWORD);
  }

  public Class<String> getCredentialType() {
    return String.class;
  }

  @Override
  public AuthenticationPlugin getImplementation() {
    return this;
  }

  @Override
  public void init() {
    PluginContext context = getContext();
    this.username = context.getProperty(SIMPLE_USERNAME);
    this.password = context.getProperty(SIMPLE_PASSWORD);
  }

  public String getName() {
    return "simple";
  }

  public AuthInfo authenticate(String username, String password)
    throws Exception {
    return new AuthInfo() {
      @Override
      public boolean isSuccessful() {
        return SimpleAuthenticationPlugin.this.username != null &&
                SimpleAuthenticationPlugin.this.password != null &&
                SimpleAuthenticationPlugin.this.username.equals(username) &&
                SimpleAuthenticationPlugin.this.password.equals(password);
      }
    };
  }
}
