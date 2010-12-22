/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.crsh.shell.impl;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.crsh.command.CommandContext;
import org.crsh.command.CommandProvider;
import org.crsh.command.ScriptException;
import org.crsh.command.ShellCommand;
import org.crsh.shell.*;
import org.crsh.shell.io.ShellPrinter;
import org.crsh.util.TimestampedObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class CRaSH implements Shell {

  /** . */
  private static final Logger log = LoggerFactory.getLogger(CRaSH.class);

  /** . */
  private final GroovyShell groovyShell;

  /** . */
  private final ShellContext context;

  /** . */
  private final Map<String, TimestampedObject<CommandProvider>> commands;

  /** . */
  final Map<String, Object> attributes;

  CommandProvider getCommand(String name) {
    TimestampedObject<CommandProvider> provider = commands.get(name);

    //
    Resource script = context.loadResource(name, ResourceKind.SCRIPT);

    //
    if (script != null) {
      if (provider != null) {
        if (script.getTimestamp() != provider.getTimestamp()) {
          provider = null;
        }
      }

      //
      if (provider == null) {
        Class<?> clazz = groovyShell.getClassLoader().parseClass(script.getContent(), name);
        if (ShellCommand.class.isAssignableFrom(clazz)) {
          Class<ShellCommand<Object, Object>> commandClazz = (Class<ShellCommand<Object, Object>>)clazz.asSubclass(ShellCommand.class);
          TimestampedObject a = wrap(script.getTimestamp(), commandClazz);
          provider = a;
          commands.put(name, provider);
        } else if (CommandProvider.class.isAssignableFrom(clazz)) {
          throw new UnsupportedOperationException("todo");
        } else {
          log.error("Parsed script does not implements " + ShellCommand.class.getName());
        }
      }
    }

    //
    if (provider == null) {
      return null;
    }

    //
    return provider.getObject();
  }

  private <C, P> TimestampedObject<CommandProvider> wrap(
    long timestamp,
    final Class<ShellCommand<C, P>> commandClass) {
    CommandProvider provider = new CommandProvider() {
      public ShellCommand<?, ?> create(String... args) {
        try {
          final ShellCommand<C, P> command = commandClass.newInstance();
          return new ShellCommand<C, P>() {
            public void usage(ShellPrinter printer) {
              command.usage(printer);
            }
            public void execute(CommandContext<C, P> context, String... args) throws ScriptException {
              command.execute(context, args);
            }
            public Class<P> getProducedType() {
              return command.getProducedType();
            }
            public Class<C> getConsumedType() {
              return command.getConsumedType();
            }
          };
        }
        catch (InstantiationException e) {
          throw new Error(e);
        }
        catch (IllegalAccessException e) {
          throw new Error(e);
        }
      }
    };
    return new TimestampedObject<CommandProvider>(timestamp, provider);
  }

  public GroovyShell getGroovyShell() {
    return groovyShell;
  }

  public Object getAttribute(String name) {
    return attributes.get(name);
  }

  public void setAttribute(String name, Object value) {
    attributes.put(name, value);
  }

  public CRaSH(final ShellContext context) {
    HashMap<String, Object> attributes = new HashMap<String, Object>();

    // Set context available to scripts
    attributes.put("shellContext", context);

    //
    CompilerConfiguration config = new CompilerConfiguration();
    config.setRecompileGroovySource(true);
    config.setScriptBaseClass(GroovyScriptCommand.class.getName());
    GroovyShell groovyShell = new GroovyShell(context.getLoader(), new Binding(attributes), config);

    // Evaluate login script
    String script = context.loadResource("login", ResourceKind.LIFECYCLE).getContent();
    groovyShell.evaluate(script, "login");

    //
    this.attributes = attributes;
    this.groovyShell = groovyShell;
    this.commands = new ConcurrentHashMap<String, TimestampedObject<CommandProvider>>();
    this.context = context;
  }

  public void close() {
    // Evaluate logout script
    String script = context.loadResource("logout", ResourceKind.LIFECYCLE).getContent();
    groovyShell.evaluate(script, "logout");
  }

  // Shell implementation **********************************************************************************************

  public String getWelcome() {
    return groovyShell.evaluate("welcome();").toString();
  }

  public String getPrompt() {
    return (String)groovyShell.evaluate("prompt();");
  }

  public void process(String request, ShellProcessContext processContext) {
    if (processContext == null) {
      throw new NullPointerException();
    }

    //
    log.debug("Invoking request " + request);

    //
    CommandExecution cmdExe = new CommandExecution(this, request, processContext);

    //
    cmdExe.execute();
  }

  /**
   * For now basic implementation
   */
  public List<String> complete(String prefix) {
    System.out.println("want prefix of " + prefix);
    prefix = prefix.trim();
    int pos = prefix.indexOf(' ');
    List<String> completions;
    if (pos == -1) {
      completions = new ArrayList<String>();
      for (String resourceId : context.listResourceId(ResourceKind.SCRIPT)) {
        if (resourceId.startsWith(prefix)) {
          completions.add(resourceId.substring(prefix.length()));
        }
      }
    } else {
      completions = Collections.emptyList();
    }
    System.out.println("Found completions " + completions);
    return completions;
  }
}
