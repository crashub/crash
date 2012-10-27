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

import org.crsh.RenderingContext;
import org.crsh.command.CommandInvoker;
import org.crsh.command.InvocationContext;
import org.crsh.command.ScriptException;
import org.crsh.text.Chunk;
import org.crsh.text.RenderPrintWriter;

import java.io.IOException;
import java.util.Map;

class PipeProxy<C, P> implements PipeFilter<C, P> {

  /** . */
  final CommandInvoker<C, P> command;

  /** . */
  private InvocationContext<P> context;

  /** . */
  private RenderPrintWriter writer;

  PipeProxy(CommandInvoker<C, P> command) {
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

  public RenderPrintWriter getWriter() {
    if (writer == null) {
      writer = new RenderPrintWriter(new RenderingContext<Chunk>() {
        public int getWidth() {
          return PipeProxy.this.getWidth();
        }
        public int getHeight() {
          return PipeProxy.this.getHeight();
        }
        public Class<Chunk> getConsumedType() {
          return Chunk.class;
        }
        public void provide(Chunk element) throws IOException {
          if (command.getConsumedType().isInstance(element)) {
            C consumed = command.getConsumedType().cast(element);
            PipeProxy.this.provide(consumed);
          }
        }
        public void flush() throws IOException {
          PipeProxy.this.flush();
        }
      });
    }
    return writer;
  }

  public CommandInvoker<?, ?> resolve(String s) throws ScriptException, IOException {
    return context.resolve(s);
  }

  public Map<String, Object> getSession() {
    return context.getSession();
  }

  public Map<String, Object> getAttributes() {
    return context.getAttributes();
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

  public void open(InvocationContext<P> context) {

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
