/*
 * Copyright (C) 2012 eXo Platform SAS.
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

package org.crsh.groovy;

import groovy.lang.GroovyObject;
import groovy.lang.MetaClass;
import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.crsh.command.BaseCommand;
import org.crsh.lang.impl.groovy.Helper;
import org.crsh.command.InvocationContext;
import org.crsh.command.ScriptException;
import org.crsh.lang.impl.groovy.closure.PipeLineClosure;

/**
 * The base command for Groovy class based commands.
 */
public abstract class GroovyCommand extends BaseCommand implements GroovyObject {

  // never persist the MetaClass
  private transient MetaClass metaClass;

  protected GroovyCommand() {
    this.metaClass = InvokerHelper.getMetaClass(this.getClass());
  }

  public static ScriptException unwrap(groovy.util.ScriptException cause) {
    // Special handling for groovy.util.ScriptException
    // which may be thrown by scripts because it is imported by default
    // by groovy imports
    String msg = cause.getMessage();
    ScriptException translated;
    if (msg != null) {
      translated = new ScriptException(msg);
    } else {
      translated = new ScriptException();
    }
    translated.setStackTrace(cause.getStackTrace());
    return translated;
  }

  public static Exception unwrap(Exception cause) {
    if (cause instanceof groovy.util.ScriptException) {
      return unwrap((groovy.util.ScriptException)cause);
    } else {
      return cause;
    }
  }

  public final Object invokeMethod(String name, Object args) {
    try {
      return getMetaClass().invokeMethod(this, name, args);
    }
    catch (MissingMethodException missing) {
      return Helper.invokeMethod(context, name, args, missing);
    }
  }

  public final Object getProperty(String property) {
    if (context instanceof InvocationContext<?>) {
      PipeLineClosure ret = Helper.resolveProperty((InvocationContext)context, property);
      if (ret != null) {
        return ret;
      }
    }
    try {
      return getMetaClass().getProperty(this, property);
    }
    catch (MissingPropertyException e) {
      return context.getSession().get(property);
    }
  }

  public final void setProperty(String property, Object newValue) {
    try {
      getMetaClass().setProperty(this, property, newValue);
    }
    catch (MissingPropertyException e) {
      context.getSession().put(property, newValue);
    }
  }

  public MetaClass getMetaClass() {
    if (metaClass == null) {
      metaClass = InvokerHelper.getMetaClass(getClass());
    }
    return metaClass;
  }

  public void setMetaClass(MetaClass metaClass) {
    this.metaClass = metaClass;
  }
}
