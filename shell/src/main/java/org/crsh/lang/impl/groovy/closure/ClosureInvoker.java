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
package org.crsh.lang.impl.groovy.closure;

import groovy.lang.Closure;
import org.crsh.command.CommandContext;
import org.crsh.shell.ErrorKind;
import org.crsh.shell.impl.command.spi.CommandException;
import org.crsh.shell.impl.command.spi.CommandInvoker;

import java.io.IOException;

/** @author Julien Viet */
public class ClosureInvoker extends CommandInvoker<Object, Object> {

  /** . */
  private final Closure closure;

  /** . */
  private final Class<?> type;

  /** . */
  private CommandContext<? super Object> consumer;

  public ClosureInvoker(Closure closure) {

    // Resolve the closure consumed type
    final Class<?> type;
    Class[] parameterTypes = closure.getParameterTypes();
    if (parameterTypes != null && parameterTypes.length > 0) {
      type = parameterTypes[0];
    } else {
      type = Object.class;
    }

    //
    this.type = type;
    this.closure = closure;
  }

  public Class<Object> getProducedType() {
    return Object.class;
  }

  public Class<Object> getConsumedType() {
    return Object.class;
  }

  public void provide(Object element) throws IOException, CommandException {
    if (type.isInstance(element)) {
      Object ret = closure.call(element);
      if (ret != null) {
        try {
          consumer.provide(ret);
        }
        catch (Exception e) {
          throw new CommandException(ErrorKind.EVALUATION, e);
        }
      }
    }
  }

  public void open(CommandContext<? super Object> consumer) {
    this.consumer = consumer;
    ClosureDelegate delegate = new ClosureDelegate(consumer, closure.getOwner());
    closure.setResolveStrategy(Closure.DELEGATE_FIRST);
    closure.setDelegate(delegate);
  }

  public void flush() throws IOException {
    consumer.flush();
  }

  public void close() {
    consumer = null;
  }
}
