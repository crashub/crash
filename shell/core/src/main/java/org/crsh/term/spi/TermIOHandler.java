package org.crsh.term.spi;

import java.security.Principal;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public interface TermIOHandler {

  /**
   * Handle an IO for the specified termi IO and user principal.
   *
   * @param io the io
   * @param user the principal
   */
  void handle(TermIO io, Principal user);

}
