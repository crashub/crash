package org.crsh.shell.impl;

import groovy.lang.Script;
import org.crsh.command.GroovyScriptCommand;
import org.crsh.command.ShellCommand;
import org.crsh.plugin.PluginContext;
import org.crsh.plugin.ResourceKind;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class CRaSH {


  /** . */
  final ClassManager<ShellCommand> commands;

  /** . */
  final ClassManager<Script> lifecycles;

  /** . */
  final PluginContext context;

  public CRaSH(PluginContext context) {
    this.context = context;
    this.commands = new ClassManager<ShellCommand>(context, ResourceKind.COMMAND, ShellCommand.class, GroovyScriptCommand.class);
    this.lifecycles = new ClassManager<Script>(context, ResourceKind.LIFECYCLE, Script.class, Script.class);
  }

  public CRaSHSession createSession() {
    return new CRaSHSession(this);
  }
}
