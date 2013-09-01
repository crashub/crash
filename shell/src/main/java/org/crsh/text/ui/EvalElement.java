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
import org.crsh.command.CRaSHCommand;
import org.crsh.command.CommandInvoker;
import org.crsh.lang.groovy.command.GroovyScriptCommand;
import org.crsh.command.InvocationContext;
import org.crsh.command.ScriptException;
import org.crsh.text.Chunk;
import org.crsh.text.RenderPrintWriter;
import org.crsh.text.Renderable;
import org.crsh.text.Renderer;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;

public class EvalElement extends Element {

  /** The closure to evaluate. */
  Closure closure;

  public Renderer renderer() {

    Object owner = closure.getOwner();

    //
    final InvocationContext ctx;
    Object cmd;
    while (true) {
      if (owner instanceof CRaSHCommand) {
        cmd = owner;
        ctx = ((CRaSHCommand)cmd).peekContext();
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
    final LinkedList<Renderer> renderers = new LinkedList<Renderer>();

    //
    final InvocationContext nested = new InvocationContext() {

      /** . */
      private LinkedList<Object> buffer = new LinkedList<Object>();

      /** . */
      private Renderable renderable;

      public CommandInvoker<?, ?> resolve(String s) throws ScriptException, IOException {
        return ctx.resolve(s);
      }

      public boolean isPiped() {
        throw new UnsupportedOperationException();
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

      public void write(Chunk chunk) throws IOException {
        provide(chunk);
      }

      public void provide(Object element) throws IOException {
        Renderable current = Renderable.getRenderable(element.getClass());
        if (current == null) {
          current = Renderable.ANY;
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
          Renderer i = renderable.renderer(buffer.iterator());
          buffer.clear();
          renderers.add(i);
        }
      }

      public void close() throws IOException {
        // Nothing to do, except maybe release resources (and also prevent to do any other operation)
      }
    };

    if (cmd instanceof CRaSHCommand) {
      ((CRaSHCommand)cmd).pushContext(nested);
    } else {
      ((GroovyScriptCommand)cmd).pushContext(nested);
    }
    try {
      closure.call();
    }
    finally {
      if (cmd instanceof CRaSHCommand) {
        ((CRaSHCommand)cmd).popContext();
      } else {
        ((GroovyScriptCommand)cmd).popContext();
      }
    }

    // Be sure to flush
    try {
      nested.flush();
    }
    catch (IOException e) {
      e.printStackTrace();
    }

    //
    return Renderer.vertical(renderers);
  }
}
