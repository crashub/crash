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

package org.crsh.command.pipeline;

import org.crsh.command.CommandContext;
import org.crsh.command.ScriptException;
import org.crsh.shell.InteractionContext;
import org.crsh.io.Filter;

import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class Pipe<C, P> implements Filter<C, P, CommandContext<P>>, CommandContext<C> {

  /** . */
  protected CommandContext<P> context;

  /** . */
  final Filter<C, P, InteractionContext<P>> command;

  Pipe(Filter<C, P, InteractionContext<P>> command) {
    this.command = command;
  }

  public final boolean takeAlternateBuffer() throws IOException {
    return context.takeAlternateBuffer();
  }

  public final boolean releaseAlternateBuffer() throws IOException {
    return context.releaseAlternateBuffer();
  }

  public final String getProperty(String propertyName) {
    return context.getProperty(propertyName);
  }

  public final String readLine(String msg, boolean echo) {
    return context.readLine(msg, echo);
  }

  public final int getWidth() {
    return context.getWidth();
  }

  public final int getHeight() {
    return context.getHeight();
  }

  public Map<String, Object> getSession() {
    return context.getSession();
  }

  public Map<String, Object> getAttributes() {
    return context.getAttributes();
  }

  public boolean isPiped() {
    return context.isPiped();
  }

  public Class<P> getProducedType() {
    return command.getProducedType();
  }

  public Class<C> getConsumedType() {
    return command.getConsumedType();
  }

  public void open(CommandContext<P> consumer) {
    this.context = consumer;
    this.command.open(consumer);
  }

  public void provide(C element) throws IOException {
    if (command.getConsumedType().isInstance(element)) {
      command.provide(element);
    }
  }

  public void flush() throws IOException {
    command.flush();
  }

  public void close() throws ScriptException, IOException {
    command.close();
  }
}
