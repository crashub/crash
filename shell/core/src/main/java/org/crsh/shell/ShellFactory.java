package org.crsh.shell;

import java.security.Principal;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public interface ShellFactory {

  /**
   * Create a shell object ready to be used.
   *
   * @param principal the user principal it may be null in case of an unauthenticated user
   * @return the shell instance
   */
  Shell create(Principal principal);

}
