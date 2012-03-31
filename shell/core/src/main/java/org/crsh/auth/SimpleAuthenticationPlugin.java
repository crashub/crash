package org.crsh.auth;

import org.crsh.plugin.CRaSHPlugin;
import org.crsh.plugin.PluginContext;
import org.crsh.plugin.PropertyDescriptor;

import java.util.Arrays;

/**
 * A simple authentication plugin based on user a username and password.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class SimpleAuthenticationPlugin extends
  CRaSHPlugin<AuthenticationPlugin> implements
  AuthenticationPlugin {

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
      "The password");

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

  public boolean authenticate(String username, String password)
    throws Exception {
    return this.username != null &&
      this.password != null &&
      this.username.equals(username) &&
      this.password.equals(password);
  }
}
