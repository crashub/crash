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

package org.crsh.text.ui;

import groovy.lang.Closure;
import org.crsh.command.ShellSafety;
import org.crsh.command.ShellSafetyFactory;
import org.crsh.groovy.GroovyCommand;
import org.crsh.shell.Shell;
import org.crsh.shell.impl.command.AbstractInvocationContext;
import org.crsh.shell.impl.command.spi.CommandException;
import org.crsh.text.Screenable;
import org.crsh.shell.impl.command.spi.CommandInvoker;
import org.crsh.lang.impl.groovy.command.GroovyScriptCommand;
import org.crsh.command.InvocationContext;
import org.crsh.text.CLS;
import org.crsh.text.LineRenderer;
import org.crsh.text.RenderPrintWriter;
import org.crsh.text.Renderer;
import org.crsh.text.Style;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;

public class EvalElement extends Element {

  /** The closure to evaluate. */
  Closure closure;

  public LineRenderer renderer() {

    Object owner = closure.getOwner();

    //
    final InvocationContext ctx;
    Object cmd;
    while (true) {
      if (owner instanceof GroovyCommand) {
        cmd = owner;
        ctx = ((GroovyCommand)cmd).peekContext();
        break;
      } else if (owner instanceof GroovyScriptCommand) {
        cmd = owner;
        ctx = ((GroovyScriptCommand)cmd).peekContext();
        break;
      } else if (owner instanceof Closure) {
        owner = ((Closure)owner).getOwner();
      } else {
        throw new UnsupportedOperationException("Cannot resolver owner " + owner + " to command");
      }
    }

    //
    final LinkedList<LineRenderer> renderers = new LinkedList<LineRenderer>();

    //
    final InvocationContext nested = new AbstractInvocationContext() {

      /** . */
      private LinkedList<Object> buffer = new LinkedList<Object>();

      /** . */
      private Renderer renderable;

      @Override
      public ShellSafety getShellSafety() {
        return ShellSafetyFactory.getCurrentThreadShellSafety();
      }

      public CommandInvoker<?, ?> resolve(String s) throws CommandException {
        return ctx.resolve(s);
      }

      public boolean takeAlternateBuffer() {
        return false;
      }

      public boolean releaseAlternateBuffer() {
        return false;
      }

      public RenderPrintWriter getWriter() {
        return ctx.getWriter();
      }

      public Map<String, Object> getSession() {
        return ctx.getSession();
      }

      public Map<String, Object> getAttributes() {
        return ctx.getAttributes();
      }

      public int getWidth() {
        return ctx.getWidth();
      }

      public int getHeight() {
        return ctx.getHeight();
      }

      public String getProperty(String propertyName) {
        return ctx.getProperty(propertyName);
      }

      public String readLine(String msg, boolean echo) {
        return null;
      }

      public Class getConsumedType() {
        return Object.class;
      }

      public Screenable append(CharSequence s) throws IOException {
        provide(s);
        return this;
      }

      @Override
      public Appendable append(char c) throws IOException {
        return append(Character.toString(c));
      }

      @Override
      public Appendable append(CharSequence csq, int start, int end) throws IOException {
        return append(csq.subSequence(start, end));
      }

      public Screenable append(Style style) throws IOException {
        provide(style);
        return this;
      }

      public Screenable cls() throws IOException {
        provide(CLS.INSTANCE);
        return this;
      }

      public void provide(Object element) throws IOException {
        Renderer current = Renderer.getRenderable(element.getClass());
        if (current == null) {
          current = Renderer.ANY;
        }
        if (current != null) {
          if (renderable != null && !current.equals(renderable)) {
            flush();
          }
          buffer.addLast(element);
          renderable = current;
        }
      }

      public void flush() throws IOException {
        // We don't really flush, we just compute renderables from the buffer
        if (buffer.size() > 0) {
          LineRenderer i = renderable.renderer(buffer.iterator());
          buffer.clear();
          renderers.add(i);
        }
      }

      public void close() throws IOException {
        // Nothing to do, except maybe release resources (and also prevent to do any other operation)
      }
    };

    if (cmd instanceof GroovyCommand) {
      ((GroovyCommand)cmd).pushContext(nested);
    } else {
      ((GroovyScriptCommand)cmd).pushContext(nested);
    }
    try {
      closure.call();
    }
    finally {
      if (cmd instanceof GroovyCommand) {
        ((GroovyCommand)cmd).popContext();
      } else {
        ((GroovyScriptCommand)cmd).popContext();
      }
    }

    // Be sure to flush
    try {
      nested.flush();
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    //
    return LineRenderer.vertical(renderers);
  }
}
