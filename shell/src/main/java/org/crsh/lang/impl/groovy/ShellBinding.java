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
package org.crsh.lang.impl.groovy;

import groovy.lang.Binding;
import org.crsh.command.CommandContext;
import org.crsh.command.InvocationContext;
import org.crsh.command.ShellSafety;
import org.crsh.command.ShellSafetyFactory;
import org.crsh.shell.impl.command.AbstractInvocationContext;
import org.crsh.shell.impl.command.spi.CommandInvoker;
import org.crsh.text.RenderPrintWriter;
import org.crsh.text.Screenable;
import org.crsh.shell.impl.command.ShellSession;
import org.crsh.shell.impl.command.spi.CommandException;
import org.crsh.shell.impl.command.InvocationContextImpl;
import org.crsh.shell.impl.command.spi.Command;
import org.crsh.lang.impl.groovy.closure.PipeLineClosure;
import org.crsh.text.Style;

import java.io.IOException;
import java.util.Map;

/** @author Julien Viet */
class ShellBinding extends Binding {

  /** . */
  private final ShellSession session;

  /** . */
  private InvocationContext<Object> current;

  public ShellBinding(Map variables, ShellSession session) {
    super(variables);

    //
    this.session = session;
  }

  private InvocationContext<Object> proxy = new AbstractInvocationContext<Object>() {
    public RenderPrintWriter getWriter() {
      if (current == null) {
        throw new IllegalStateException("Not under context");
      } else {
        // Warning we don't proxy the writer, should we ?
        return current.getWriter();
      }
    }

    @Override
    public ShellSafety getShellSafety() {
      return ShellSafetyFactory.getCurrentThreadShellSafety();
    }

    public CommandInvoker<?, ?> resolve(String s) throws CommandException {
      if (current == null) {
        throw new IllegalStateException("Not under context");
      } else {
        return current.resolve(s);
      }
    }
    public void close() throws Exception {
      if (current == null) {
        throw new IllegalStateException("Not under context");
      } else {
        current.close();
      }
    }
    public boolean takeAlternateBuffer() throws IOException {
      if (current == null) {
        throw new IllegalStateException("Not under context");
      } else {
        return current.takeAlternateBuffer();
      }
    }
    public boolean releaseAlternateBuffer() throws IOException {
      if (current == null) {
        throw new IllegalStateException("Not under context");
      } else {
        return current.releaseAlternateBuffer();
      }
    }
    public String readLine(String msg, boolean echo) throws IOException, InterruptedException {
      if (current == null) {
        throw new IllegalStateException("Not under context");
      } else {
        return current.readLine(msg, echo);
      }
    }
    public int getWidth() {
      if (current == null) {
        throw new IllegalStateException("Not under context");
      } else {
        return current.getWidth();
      }
    }
    public int getHeight() {
      if (current == null) {
        throw new IllegalStateException("Not under context");
      } else {
        return current.getWidth();
      }
    }
    public void provide(Object element) throws Exception {
      if (current == null) {
        throw new IllegalStateException("Not under context");
      } else {
        current.provide(element);
      }
    }
    public Class<Object> getConsumedType() {
      if (current == null) {
        throw new IllegalStateException("Not under context");
      } else {
        return current.getConsumedType();
      }
    }
    public void flush() throws IOException {
      if (current == null) {
        throw new IllegalStateException("Not under context");
      } else {
        current.flush();
      }
    }
    public String getProperty(String propertyName) {
      if (current == null) {
        throw new IllegalStateException("Not under context");
      } else {
        return current.getProperty(propertyName);
      }
    }
    public Map<String, Object> getSession() {
      if (current == null) {
        throw new IllegalStateException("Not under context");
      } else {
        return current.getSession();
      }
    }
    public Map<String, Object> getAttributes() {
      if (current == null) {
        throw new IllegalStateException("Not under context");
      } else {
        return current.getAttributes();
      }
    }
    public Screenable append(CharSequence s) throws IOException {
      if (current == null) {
        throw new IllegalStateException("Not under context");
      } else {
        current.append(s);
      }
      return this;
    }
    public Appendable append(char c) throws IOException {
      if (current == null) {
        throw new IllegalStateException("Not under context");
      } else {
        current.append(c);
      }
      return this;
    }
    public Appendable append(CharSequence csq, int start, int end) throws IOException {
      if (current == null) {
        throw new IllegalStateException("Not under context");
      } else {
        current.append(csq, start, end);
      }
      return this;
    }
    public Screenable append(Style style) throws IOException {
      if (current == null) {
        throw new IllegalStateException("Not under context");
      } else {
        current.append(style);
      }
      return this;
    }
    public Screenable cls() throws IOException {
      if (current == null) {
        throw new IllegalStateException("Not under context");
      } else {
        current.cls();
      }
      return this;
    }
  };

  public InvocationContext<Object> getCurrent() {
    return current;
  }

  public void setCurrent(InvocationContext<Object> current) {
    this.current = current;
  }

  @Override
  public Object getVariable(String name) {
    if (name.equals("context")) {
      return proxy;
    } else {
      if (session != null) {
        try {
          Command<?> cmd = session.getCommand(name);
          if (cmd != null) {
            return new PipeLineClosure(new InvocationContextImpl<Object>(proxy, ShellSafetyFactory.getCurrentThreadShellSafety()), name, cmd);
          }
        } catch (CommandException ignore) {
          // Really ?
        }
        return super.getVariable(name);
      } else {
        return super.getVariable(name);
      }
    }
  }
}
