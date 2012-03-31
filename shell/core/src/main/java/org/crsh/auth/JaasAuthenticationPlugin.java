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

/**
 * A jaas plugin for authentication purpose
 *
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class JaasAuthenticationPlugin extends CRaSHPlugin<AuthenticationPlugin> implements AuthenticationPlugin {

  /** . */
  static final PropertyDescriptor<String> JAAS_DOMAIN = PropertyDescriptor.create("auth.jaas.domain", (String)null, "The JAAS domain name used for authentication");

  public String getName() {
    return "jaas";
  }

  @Override
  protected Iterable<PropertyDescriptor<?>> createConfigurationCapabilities() {
    return Collections.<PropertyDescriptor<?>>singletonList(JAAS_DOMAIN);
  }

  public boolean authenticate(final String username, final String password) throws Exception {
    String domain = getContext().getProperty(JAAS_DOMAIN);
    if (domain != null) {
      log.debug("Will use the JAAS domain '" + domain + "' for authenticating user " + username);
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
        log.debug("Authenticated user " + username + " against the JAAS domain '" + domain + "'");
        return true;
      }
      catch (Exception e) {
        if (log.isDebugEnabled()) log.error("Exception when authenticating user " + username + " to JAAS domain '" + domain + "'", e);
        return false;
      }
    }
    else {
      log.warn("The JAAS domain property '" + JAAS_DOMAIN.name + "' was not found");
      return false;
    }
  }

  @Override
  public AuthenticationPlugin getImplementation() {
    return this;
  }
}
