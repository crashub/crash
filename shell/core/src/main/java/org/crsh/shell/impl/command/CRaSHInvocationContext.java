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

import org.crsh.Pipe;
import org.crsh.command.BaseCommandContext;
import org.crsh.command.InvocationContext;
import org.crsh.shell.ShellProcessContext;
import org.crsh.text.Chunk;
import org.crsh.text.RenderContext;
import org.crsh.text.RenderPrintWriter;

import java.io.IOException;
import java.util.Map;

public class CRaSHInvocationContext<P> extends BaseCommandContext implements InvocationContext<P> {

  /** . */
  private final ShellProcessContext processContext;

  /** . */
  private RenderPrintWriter writer;

  /** . */
  protected Pipe<Object> producer;

  protected CRaSHInvocationContext(
      ShellProcessContext processContext,
      Map<String, Object> session,
      Map<String, Object> attributes,
      Pipe<Object> producer) {
    super(session, attributes);

    //
    this.processContext = processContext;
    this.producer = producer;
  }

  public int getWidth() {
    return processContext.getWidth();
  }

  public String getProperty(String propertyName) {
    return processContext.getProperty(propertyName);
  }

  public String readLine(String msg, boolean echo) {
    return processContext.readLine(msg, echo);
  }

  public void provide(P element) throws IOException {
    producer.provide(element);
  }

  public void flush() throws IOException {
    producer.flush();
  }

  public RenderPrintWriter getWriter() {
    if (writer == null) {
      writer = new RenderPrintWriter(new RenderContext() {
        public int getWidth() {
          return processContext.getWidth();
        }
        public void provide(Chunk element) throws IOException {
          producer.provide(element);
        }
        public void flush() throws IOException {
          producer.flush();
        }
      });
    }
    return writer;
  }
}
