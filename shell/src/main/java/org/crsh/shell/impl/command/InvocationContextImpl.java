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

import org.crsh.command.CommandContext;
import org.crsh.command.ShellSafety;
import org.crsh.lang.impl.script.CommandNotFoundException;
import org.crsh.shell.ErrorKind;
import org.crsh.text.Screenable;
import org.crsh.shell.impl.command.spi.CommandException;
import org.crsh.lang.impl.script.Token;
import org.crsh.text.ScreenContext;
import org.crsh.lang.impl.script.PipeLineFactory;
import org.crsh.shell.impl.command.spi.CommandInvoker;
import org.crsh.text.RenderPrintWriter;
import org.crsh.text.Style;

import java.io.IOException;
import java.util.Map;

public final class InvocationContextImpl<P> extends AbstractInvocationContext<P> {

  /** . */
  private static final int WRITTEN = 0;

  /** . */
  private static final int FLUSHED = 1;

  /** . */
  private static final int CLOSED = 2;

  /** . */
  private final CommandContext<P> commandContext;

  /** . */
  private RenderPrintWriter writer;

  /** . */
  int status;
  ShellSafety shellSafety = null;

  @Override
  public ShellSafety getShellSafety() {
    return shellSafety;
  }

  public InvocationContextImpl(CommandContext<P> commandContext, ShellSafety shellSafety) {
    this.commandContext = commandContext;
    this.shellSafety = shellSafety;
    this.status = FLUSHED;
  }

  public RenderPrintWriter getWriter() {
    if (writer == null) {
      writer = new RenderPrintWriter(new ScreenContext() {
        public int getWidth() {
          return InvocationContextImpl.this.getWidth();
        }
        public int getHeight() {
          return InvocationContextImpl.this.getHeight();
        }
        public Screenable append(CharSequence s) throws IOException {
          InvocationContextImpl.this.append(s);
          return this;
        }
        public Appendable append(char c) throws IOException {
          InvocationContextImpl.this.append(c);
          return this;
        }
        public Appendable append(CharSequence csq, int start, int end) throws IOException {
          InvocationContextImpl.this.append(csq, start, end);
          return this;
        }
        public Screenable append(Style style) throws IOException {
          InvocationContextImpl.this.append(style);
          return this;
        }
        public Screenable cls() throws IOException {
          InvocationContextImpl.this.cls();
          return this;
        }
        public void flush() throws IOException {
          InvocationContextImpl.this.flush();
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

  public CommandInvoker<?, ?> resolve(String s) throws CommandException {
    CRaSHSession session = (CRaSHSession)getSession();
    Token token2 = Token.parse(s);
    try {
      PipeLineFactory factory = token2.createFactory();
      return factory.create(session);
    }
    catch (CommandNotFoundException e) {
      throw new CommandException(ErrorKind.SYNTAX, e.getMessage(), e);
    }
  }

  public Class<P> getConsumedType() {
    return commandContext.getConsumedType();
  }

  public String getProperty(String propertyName) {
    return commandContext.getProperty(propertyName);
  }

  public String readLine(String msg, boolean echo) throws IOException, InterruptedException {
    return commandContext.readLine(msg, echo);
  }

  public int getWidth() {
    return commandContext.getWidth();
  }

  public int getHeight() {
    return commandContext.getHeight();
  }

  public Screenable append(CharSequence s) throws IOException {
    if (status != CLOSED) {
      status = WRITTEN;
      commandContext.append(s);
    }
    return this;
  }

  public Screenable append(char c) throws IOException {
    if (status != CLOSED) {
      status = WRITTEN;
      commandContext.append(c);
    }
    return this;
  }

  public Screenable append(CharSequence csq, int start, int end) throws IOException {
    if (status != CLOSED) {
      status = WRITTEN;
      commandContext.append(csq, start, end);
    }
    return this;
  }

  public Screenable append(Style style) throws IOException {
    if (status != CLOSED) {
      status = WRITTEN;
      commandContext.append(style);
    }
    return this;
  }

  public Screenable cls() throws IOException {
    if (status != CLOSED) {
      status = WRITTEN;
      commandContext.cls();
    }
    return this;
  }

  public void provide(P element) throws Exception {
    if (status != CLOSED) {
      status = WRITTEN;
      commandContext.provide(element);
    }
  }

  public void flush() throws IOException {
    if (status == WRITTEN) {
      status = FLUSHED;
      commandContext.flush();
    }
  }

  public void close() throws Exception {
    if (status != CLOSED) {
      try {
        flush();
      }
      catch (Exception e) {
        // Ignore ?
      }
      status = CLOSED;
      commandContext.close();
    }
  }

  public Map<String, Object> getSession() {
    return commandContext.getSession();
  }

  public Map<String, Object> getAttributes() {
    return commandContext.getAttributes();
  }
}
