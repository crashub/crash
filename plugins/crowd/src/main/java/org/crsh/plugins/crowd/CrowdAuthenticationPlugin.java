package org.crsh.plugins.crowd;

import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.integration.rest.service.factory.RestCrowdClientFactory;
import com.atlassian.crowd.service.client.ClientProperties;
import com.atlassian.crowd.service.client.ClientPropertiesImpl;
import com.atlassian.crowd.service.client.ClientResourceLocator;
import com.atlassian.crowd.service.client.CrowdClient;
import org.crsh.auth.AuthenticationPlugin;
import org.crsh.plugin.CRaSHPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows to use an Atlassian Crowd serer to authenticate on CRaSH
 * To use it you need to :
 * <ul>
 *   <li>Define the application on the crowd server side,</li>
 *   <li>Use <pre>crash.auth=crowd</pre> in your crash.properties configuration file,</li>
 *   <li>Create the <a href="https://confluence.atlassian.com/display/CROWD/The+crowd.properties+File"><pre>crowd.properties</pre> configuration file</a>
 *   and add it in your application classpath or by defining its path with the system property crowd.properties (<pre>-Dcrowd.properties={FILE-PATH}/crowd.properties</pre>).</li>
 * </ul>
 */
public class CrowdAuthenticationPlugin extends
    CRaSHPlugin<AuthenticationPlugin> implements
    AuthenticationPlugin {

  /**
   * Logger
   */
  protected final Logger log = LoggerFactory.getLogger(getClass());

  /**
   * Crowd client instance
   */
  private static volatile CrowdClient crowdClient;

  /**
   * Lock to create the crowd client
   */
  private static final Object lock = new Object();

  /**
   * Get a ready to use CrowdClient.
   *
   * @return a CrowdClient already initialized
   */
  private static CrowdClient getCrowdClient() {
    if (crowdClient == null) {
      synchronized (lock) {
        if (crowdClient == null) {
          ClientResourceLocator
              crl = new ClientResourceLocator("crowd.properties");
          if (crl.getProperties() == null) {
            throw new NullPointerException("crowd.properties can not be found in classpath");
          }
          ClientProperties clientProperties = ClientPropertiesImpl.newInstanceFromResourceLocator(crl);
          RestCrowdClientFactory restCrowdClientFactory = new RestCrowdClientFactory();
          crowdClient = restCrowdClientFactory.newInstance(clientProperties);
        }
      }
    }
    return crowdClient;
  }

  @Override
  public String getName() {
    return "crowd";
  }

  @Override
  public boolean authenticate(String username, String password) throws Exception {
    // Username and passwords are required
    if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
      log.warn("Unable to logon without username and password.");
      return false;
    }
    try {
      // Authenticate the user
      if (log.isDebugEnabled()) log.debug("Authenticating '" + username + "' on crowd directory");
      getCrowdClient().authenticateUser(username, password);
      return true;
    } catch (InvalidAuthenticationException e) {
      log.warn("Authentication failed for user '" + username + "'");
      return false;
    } catch (ApplicationPermissionException e) {
      log.error("Application not authorized to authenticate user '" + username + "'", e);
      return false;
    }
  }

  @Override
  public AuthenticationPlugin getImplementation() {
    return this;
  }
}
