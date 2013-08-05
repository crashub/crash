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

package org.crsh.lang.groovy.closure;

import groovy.lang.Closure;
import groovy.lang.MissingMethodException;
import groovy.lang.Tuple;
import org.codehaus.groovy.runtime.MetaClassHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CommandClosure extends Closure {

  /** . */
  protected HashMap<String, Object> options;

  /** . */
  protected ArrayList<Object> args;

  public CommandClosure(Object owner) {
    super(owner);

    //
    this.options = null;
    this.args = null;
  }

  public static Object[] unwrapArgs(Object arguments) {
    if (arguments == null) {
      return MetaClassHelper.EMPTY_ARRAY;
    } else if (arguments instanceof Tuple) {
      Tuple tuple = (Tuple) arguments;
      return tuple.toArray();
    } else if (arguments instanceof Object[]) {
      return (Object[])arguments;
    } else {
      return new Object[]{arguments};
    }
  }

  @Override
  public Object invokeMethod(String name, Object args) {
    try {
      return super.invokeMethod(name, args);
    }
    catch (MissingMethodException e) {
      if ("with".equals(name)) {
        Object[] array = unwrapArgs(args);
        if (array.length == 0) {
          return this;
        } else if (array[0] instanceof Map) {
          Map options = (Map)array[0];
          if( array.length > 1) {
            return options(options, Arrays.copyOfRange(array, 1, array.length));
          } else {
            return options(options, null);
          }
        } else {
          return options(null, array);
        }
      } else {
        throw e;
      }
    }
  }

  private CommandClosure options(Map<?, ?> options, Object[] arguments) {
    CommandClosure ret;
    if (this instanceof MethodDispatcher) {
      ret = new MethodDispatcher(((MethodDispatcher)this).dispatcher, ((MethodDispatcher)this).name);
    } else {
      ret = new ClassDispatcher(((ClassDispatcher)this).command, ((ClassDispatcher)this).owner);
    }

    // We merge options
    if (options != null && options.size() > 0) {
      if (this.options == null) {
        ret.options = new HashMap<String, Object>();
      } else {
        ret.options = new HashMap<String, Object>(this.options);
      }
      for (Map.Entry<?, ?> arg : options.entrySet()) {
        ret.options.put(arg.getKey().toString(), arg.getValue());
      }
    }

    // We replace arguments
    if (arguments != null) {
      ret.args = new ArrayList<Object>(Arrays.asList(arguments));
    }

    //
    return ret;
  }
}
