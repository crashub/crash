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

package org.crsh.cli.impl.completion;

import org.crsh.cli.impl.Delimiter;
import org.crsh.cli.completers.EmptyCompleter;
import org.crsh.cli.descriptor.ParameterDescriptor;
import org.crsh.cli.spi.Completer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

class ParameterCompletion extends Completion {

  /** . */
  private final String prefix;

  /** . */
  private final Delimiter delimiter;

  /** . */
  private final ParameterDescriptor parameter;

  /** . */
  private final Completer completer;

  public ParameterCompletion(String prefix, Delimiter delimiter, ParameterDescriptor parameter, Completer completer) {
    this.prefix = prefix;
    this.delimiter = delimiter;
    this.parameter = parameter;
    this.completer = completer;
  }

  public CompletionMatch complete() throws CompletionException {

    //
    Class<? extends Completer> completerType = parameter.getCompleterType();
    Completer completer = null;
    if (completerType != EmptyCompleter.class) {

      // If the provided completer instance matches the parameter completer type
      // then we use it
      if (completerType.isInstance(this.completer)) {
        completer = this.completer;
      } else {
        // Otherwise we instantiate it
        Constructor<? extends Completer> ctor;
        try {
          ctor = completerType.getDeclaredConstructor();
        }
        catch (NoSuchMethodException ignore) {
          throw new CompletionException("The completer " + completerType.getName() + " does not provide a no arg constructor");
        }
        if (Modifier.isPublic(ctor.getModifiers())) {
          try {
            completer = ctor.newInstance();
          }
          catch (InstantiationException e) {
            throw new CompletionException("The completer " + completerType.getName() + " cannot be abstract");
          }
          catch (InvocationTargetException e) {
            throw new CompletionException(e.getCause());
          }
          catch (Exception e) {
            throw new CompletionException(e);
          }
        } else {
          throw new CompletionException("The completer " + completerType.getName() + " constructor must be public");
        }
      }
    }

    //
    if (completer != null) {
      try {
        return new CompletionMatch(delimiter, completer.complete(parameter, prefix));
      }
      catch (Exception e) {
        throw new CompletionException(e);
      }
    } else {
      return new CompletionMatch(delimiter, org.crsh.cli.spi.Completion.create());
    }
  }
}
