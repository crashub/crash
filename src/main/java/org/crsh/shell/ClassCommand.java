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
package org.crsh.shell;

import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class ClassCommand extends GroovyObjectSupport implements ShellCommand {

  /** . */
  @Argument
  protected final List<String> arguments = new ArrayList<String>();

  /** . */
  private CommandContext context;

  @Override
  public Object invokeMethod(String name, Object args) {
    try {
      return super.invokeMethod(name, args);
    }
    catch (MissingMethodException e) {
      Object o = context.get(name);
      if (o instanceof Closure) {
        Closure closure = (Closure)o;
        if (args instanceof Object[]) {
          Object[] array = (Object[])args;
          if (array.length == 0) {
            return closure.call();
          } else {
            return closure.call(array);
          }
        } else {
          return closure.call(args);
        }
      } else {
        throw e;
      }
    }
  }

  @Override
  public Object getProperty(String property) {
    try {
      return super.getProperty(property);
    }
    catch (MissingPropertyException e) {
      return context.get(property);
    }
  }

  @Override
  public void setProperty(String property, Object newValue) {
    try {
      super.setProperty(property, newValue);
    }
    catch (MissingPropertyException e) {
      context.put(property, newValue);
    }
  }

  public Object execute(CommandContext context, String[] args) throws ScriptException {
    if (context == null) {
      throw new NullPointerException();
    }
    if (args == null) {
      throw new NullPointerException();
    }

    //
    CmdLineParser parser = new CmdLineParser(this);

    //
    try {
      parser.parseArgument(args);
    }
    catch (CmdLineException e) {
      throw new ScriptException("Could not parse command line", e);
    }

    //
    Object res;
    try {
      this.context = context;
      res = execute();
    }
    finally {
      this.context = null;
    }

    //
    return res;
  }

  protected abstract Object execute() throws ScriptException;

}
