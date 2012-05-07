package org.crsh.shell.impl.command;

import groovy.lang.Script;
import org.crsh.command.GroovyScriptCommand;
import org.crsh.command.NoSuchCommandException;
import org.crsh.command.ShellCommand;
import org.crsh.plugin.PluginContext;
import org.crsh.plugin.ResourceKind;

import java.security.Principal;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class CRaSH {


  /** . */
  final ClassManager<ShellCommand> commands;

  /** . */
  final ClassManager<Script> lifecycles;

  /** . */
  final PluginContext context;

  /**
   * Create a new CRaSH.
   *
   * @param context the plugin context
   * @throws NullPointerException if the context argument is null
   */
  public CRaSH(PluginContext context) throws NullPointerException {
    this.context = context;
    this.commands = new ClassManager<ShellCommand>(context, ResourceKind.COMMAND, ShellCommand.class, GroovyScriptCommand.class);
    this.lifecycles = new ClassManager<Script>(context, ResourceKind.LIFECYCLE, Script.class, Script.class);
  }

  public CRaSHSession createSession(Principal user) {
    return new CRaSHSession(this, user);
  }

  /**
   * Returns the plugin context.
   *
   * @return the plugin context
   */
  public PluginContext getContext() {
    return context;
  }

  /**
   * Attempt to obtain a command instance. Null is returned when such command does not exist.
   *
   * @param name the command name
   * @return a command instance
   * @throws org.crsh.command.NoSuchCommandException if an error occured preventing the command creation
   * @throws NullPointerException if the name argument is null
   */
  public ShellCommand getCommand(String name) throws NoSuchCommandException, NullPointerException {
    return commands.getInstance(name);
  }
}
