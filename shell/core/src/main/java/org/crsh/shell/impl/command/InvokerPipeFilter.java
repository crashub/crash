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

package org.crsh.shell.impl.command;

import org.crsh.command.CommandInvoker;
import org.crsh.command.InvocationContext;
import org.crsh.command.ScriptException;
import org.crsh.io.Filter;
import org.crsh.io.ProducerConsumer;
import org.crsh.io.ProducerContext;

import java.io.IOException;

/**
 * A pipe filter that invokes a command through a {@link CommandInvoker}.
 */
class InvokerPipeFilter<C, P> implements Filter<C, P> {

  /** . */
  final ProducerConsumer<C, P> command;

  /** . */
  private ProducerContext<P> context;

  InvokerPipeFilter(ProducerConsumer<C, P> command) {
    this.command = command;
  }

  public void invoke(InvocationContext<P> context) throws ScriptException, IOException {
    open(context);
    flush();
    close();
  }

  public void setPiped(boolean piped) {
    command.setPiped(piped);
  }

  public Class<P> getProducedType() {
    return command.getProducedType();
  }

  public Class<C> getConsumedType() {
    return command.getConsumedType();
  }

  public boolean takeAlternateBuffer() {
    return context.takeAlternateBuffer();
  }

  public boolean releaseAlternateBuffer() {
    return context.releaseAlternateBuffer();
  }

  public String getProperty(String propertyName) {
    return context.getProperty(propertyName);
  }

  public String readLine(String msg, boolean echo) {
    return context.readLine(msg, echo);
  }

  public int getWidth() {
    return context.getWidth();
  }

  public int getHeight() {
    return context.getHeight();
  }

  public void open(ProducerContext<P> context) {

    //
    this.context = context;

    // Now open command
    command.open(context);
  }

  public void provide(C element) throws IOException {
    if (command.getConsumedType().isInstance(element)) {
      command.provide(element);
    }
  }

  public void flush() throws IOException {

    // First flush the command
    command.flush();

    // Flush the next because the command may not call it
    context.flush();
  }

  public void close() throws ScriptException {
    command.close();
  }
}
