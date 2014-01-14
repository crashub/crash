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

package org.crsh.command;

import org.crsh.lang.script.Token;
import org.crsh.shell.ScreenContext;
import org.crsh.shell.impl.command.CRaSHSession;
import org.crsh.lang.script.PipeLineFactory;
import org.crsh.text.Chunk;
import org.crsh.text.RenderPrintWriter;

import java.io.IOException;
import java.util.Map;

public final class InvocationContextImpl<P> implements InvocationContext<P> {

  /** . */
  private final CommandContext<P> commandContext;

  /** . */
  private RenderPrintWriter writer;

  public InvocationContextImpl(CommandContext<P> commandContext) {
    this.commandContext = commandContext;
  }

  public boolean isPiped() {
    return commandContext.isPiped();
  }

  public RenderPrintWriter getWriter() {
    if (writer == null) {
      writer = new RenderPrintWriter(new ScreenContext() {
        public int getWidth() {
          return commandContext.getWidth();
        }
        public int getHeight() {
          return commandContext.getHeight();
        }
        public void write(Chunk chunk) throws IOException {
          commandContext.write(chunk);
        }
        public void flush() throws IOException {
          commandContext.flush();
        }
      });
    }
    return writer;
  }

  public boolean takeAlternateBuffer() throws IOException {
    return commandContext.takeAlternateBuffer();
  }

  public boolean releaseAlternateBuffer() throws IOException {
    return commandContext.releaseAlternateBuffer();
  }

  public CommandInvoker<?, ?> resolve(String s) throws ScriptException, IOException {
    CRaSHSession session = (CRaSHSession)getSession();
    Token token2 = Token.parse(s);
    PipeLineFactory factory = token2.createFactory();
    try {
      return factory.create(session);
    }
    catch (CommandCreationException e) {
      throw new ScriptException(e);
    }
  }

  public Class<P> getConsumedType() {
    return commandContext.getConsumedType();
  }

  public String getProperty(String propertyName) {
    return commandContext.getProperty(propertyName);
  }

  public String readLine(String msg, boolean echo) {
    return commandContext.readLine(msg, echo);
  }

  public int getWidth() {
    return commandContext.getWidth();
  }

  public int getHeight() {
    return commandContext.getHeight();
  }

  public void write(Chunk chunk) throws IOException {
    commandContext.write(chunk);
  }

  public void provide(P element) throws IOException {
    commandContext.provide(element);
  }

  public void flush() throws IOException {
    commandContext.flush();
  }

  public void close() throws IOException {
    commandContext.close();
  }

  public Map<String, Object> getSession() {
    return commandContext.getSession();
  }

  public Map<String, Object> getAttributes() {
    return commandContext.getAttributes();
  }

  public InvocationContextImpl<P> leftShift(Object o) throws IOException {
    if (commandContext.getConsumedType().isInstance(o)) {
      P p = commandContext.getConsumedType().cast(o);
      commandContext.provide(p);
    }
    return this;
  }
}
