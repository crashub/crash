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

package org.crsh.cmdline.invocation;

import org.crsh.cmdline.SyntaxException;
import org.crsh.cmdline.CommandDescriptor;
import org.crsh.cmdline.OptionDescriptor;
import org.crsh.cmdline.ParameterDescriptor;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class InvocationMatch<T> {

  /** . */
  private final CommandDescriptor<T> descriptor;

  /** . */
  private Map<OptionDescriptor, OptionMatch> options;

  /** . */
  private List<ArgumentMatch> arguments;

  /** . */
  private String rest;

  public InvocationMatch(CommandDescriptor<T> descriptor) {
    this.descriptor = descriptor;
    this.options = Collections.emptyMap();
    this.rest = null;
    this.arguments = Collections.emptyList();
  }

  public abstract InvocationMatch<T> owner();

  public abstract InvocationMatch<T> subordinate(String name);

  public abstract void configure(T command) throws InvocationException, SyntaxException;

  public abstract CommandInvoker<T> getInvoker();

  public CommandDescriptor<T> getDescriptor() {
    return descriptor;
  }

  public final <D extends ParameterDescriptor> ParameterMatch<D> getParameter(D parameter) {
    if (parameter instanceof OptionDescriptor) {
      return (ParameterMatch<D>)options.get(parameter);
    } else {
      for (ArgumentMatch argumentMatch : arguments) {
        if (argumentMatch.getParameter()  == parameter) {
          return (ParameterMatch<D>)argumentMatch;
        }
      }
      return null;
    }
  }

  public final CommandInvoker<T> invoke(T command) throws InvocationException, SyntaxException {
    return invoke(Resolver.EMPTY, command);
  }

  public final CommandInvoker<T> invoke(Resolver resolver, T command) throws InvocationException, SyntaxException {
    CommandInvoker<T> invoker = getInvoker();
    if (invoker != null) {
      invoker.invoke(resolver, command);
    } else {
      configure(command);
    }
    return invoker;
  }

  public Collection<OptionMatch> options() {
    return options.values();
  }

  public void option(OptionMatch option) {
    if (options.isEmpty()) {
      options = new LinkedHashMap<OptionDescriptor, OptionMatch>();
    }
    options.put(option.getParameter(), option);
  }

  public Collection<ArgumentMatch> arguments() {
    return arguments;
  }

  public void argument(ArgumentMatch argument) {
    if (arguments.isEmpty()) {
      arguments = new LinkedList<ArgumentMatch>();
    }
    arguments.add(argument);
  }

  public String getRest() {
    return rest;
  }

  public void setRest(String rest) {
    this.rest = rest;
  }
}
