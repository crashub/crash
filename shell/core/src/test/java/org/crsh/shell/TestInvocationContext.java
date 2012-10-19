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

package org.crsh.shell;

import org.crsh.Pipe;
import org.crsh.command.PipeCommand;
import org.crsh.command.CommandInvoker;
import org.crsh.command.InvocationContext;
import org.crsh.command.ShellCommand;
import org.crsh.command.BaseCommandContext;
import org.crsh.text.Chunk;
import org.crsh.text.RenderingContext;
import org.crsh.text.RenderPrintWriter;
import org.crsh.text.ChunkBuffer;

import java.io.IOException;
import java.util.*;

public class TestInvocationContext<C, P> extends BaseCommandContext implements InvocationContext<P> {

  /** . */
  protected List<P> producedItems;

  /** . */
  protected ChunkBuffer reader;

  /** . */
  protected RenderPrintWriter writer;

  /** . */
  private final Pipe<P> producer = new PipeCommand<P>() {
    public void provide(P element) throws IOException {
      if (producedItems.isEmpty()) {
        producedItems = new LinkedList<P>();
      }
      producedItems.add(element);
    }
  };

  public TestInvocationContext() {
    super(new HashMap<String, Object>(), new HashMap<String, Object>());

    //
    this.reader = null;
    this.writer = null;
    this.producedItems = Collections.emptyList();
  }

  public List<P> getProducedItems() {
    return producedItems;
  }

  public ChunkBuffer getReader() {
    return reader;
  }

  public int getWidth() {
    return 32;
  }

  public String getProperty(String propertyName) {
    return null;
  }

  public String readLine(String msg, boolean echo) {
    throw new UnsupportedOperationException();
  }

  public String execute(ShellCommand command, String... args) throws Exception {
    if (reader != null) {
      reader.clear();
    }
    StringBuilder sb = new StringBuilder();
    for (String arg : args) {
      if (sb.length() > 0) {
        sb.append(" ");
      }
      sb.append(arg);
    }
    CommandInvoker<C, P> invoker = (CommandInvoker<C, P>)command.resolveInvoker(sb.toString());
    PipeCommand<C> pc = invoker.invoke(this);
    pc.open();
    pc.close();
    return reader != null ? reader.toString() : null;
  }

  public void provide(P element) throws IOException {
    producer.provide(element);
  }

  public void flush() throws IOException {
    producer.flush();
  }

  public RenderPrintWriter getWriter() {
    if (writer == null) {
      reader = new ChunkBuffer();
      writer = new RenderPrintWriter(new RenderingContext() {
        public int getWidth() {
          return TestInvocationContext.this.getWidth();
        }
        public void provide(Chunk element) throws IOException {
          reader.provide(element);
        }
        public void flush() throws IOException {
          reader.flush();
        }
      });
    }
    return writer;
  }
}
