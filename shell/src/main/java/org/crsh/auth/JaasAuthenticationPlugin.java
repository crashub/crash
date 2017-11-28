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
import org.crsh.plugin.PropertyDescriptor;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import java.io.IOException;
import java.util.Collections;
import java.util.logging.Level;

public class JaasAuthenticationPlugin extends CRaSHPlugin<AuthenticationPlugin> implements AuthenticationPlugin<String> {

  /** . */
  static final PropertyDescriptor<String> JAAS_DOMAIN = PropertyDescriptor.create("auth.jaas.domain", (String)null, "The JAAS domain name used for authentication");

  public String getName() {
    return "jaas";
  }

  @Override
  protected Iterable<PropertyDescriptor<?>> createConfigurationCapabilities() {
    return Collections.<PropertyDescriptor<?>>singletonList(JAAS_DOMAIN);
  }

  public Class<String> getCredentialType() {
    return String.class;
  }

  public AuthInfo authenticate(final String username, final String password) throws Exception {
    String domain = getContext().getProperty(JAAS_DOMAIN);
    if (domain != null) {
      log.log(Level.FINE, "Will use the JAAS domain '" + domain + "' for authenticating user " + username);
      LoginContext loginContext = new LoginContext(domain, new Subject(), new CallbackHandler() {
        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
          for (Callback c : callbacks) {
            if (c instanceof NameCallback) {
              ((NameCallback)c).setName(username);
            }
            else if (c instanceof PasswordCallback) {
              ((PasswordCallback)c).setPassword(password.toCharArray());
            }
            else {
              throw new UnsupportedCallbackException(c);
            }
          }
        }
      });

      //
      try {
        loginContext.login();
        loginContext.logout();
        log.log(Level.FINE, "Authenticated user " + username + " against the JAAS domain '" + domain + "'");
        return AuthInfo.SUCCESSFUL;
      }
      catch (Exception e) {
        if (log.isLoggable(Level.FINE)) {
          log.log(Level.SEVERE, "Exception when authenticating user " + username + " to JAAS domain '" + domain + "'", e);
        }
        return AuthInfo.UNSUCCESSFUL;
      }
    }
    else {
      log.log(Level.WARNING, "The JAAS domain property '" + JAAS_DOMAIN.name + "' was not found");
      return AuthInfo.UNSUCCESSFUL;
    }
  }

  @Override
  public AuthenticationPlugin getImplementation() {
    return this;
  }
}
