package org.crsh.shell.impl.command;

import org.crsh.plugin.CRaSHPlugin;
import org.crsh.plugin.PluginContext;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellFactory;

import java.security.Principal;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class CRaSHShellFactory extends CRaSHPlugin<ShellFactory> implements ShellFactory {

  /** . */
  private CRaSH crash;

  public CRaSHShellFactory() {
  }

  @Override
  public void init() {
    PluginContext context = getContext();
    crash = new CRaSH(context);
  }

  @Override
  public ShellFactory getImplementation() {
    return this;
  }

  public Shell create(Principal principal) {
    return crash.createSession(principal);
  }
}
