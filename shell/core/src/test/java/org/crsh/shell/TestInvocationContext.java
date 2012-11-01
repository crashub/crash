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

import org.crsh.io.Pipe;
import org.crsh.command.CommandInvoker;
import org.crsh.command.ScriptException;
import org.crsh.command.ShellCommand;
import org.crsh.command.BaseCommandContext;
import org.crsh.io.ProducerContext;
import org.crsh.text.Chunk;
import org.crsh.text.RenderPrintWriter;
import org.crsh.text.ChunkBuffer;

import java.io.IOException;
import java.util.*;

public class TestInvocationContext<C> extends BaseCommandContext implements ProducerContext<Object> {

  /** . */
  protected List<Object> producedItems;

  /** . */
  protected ChunkBuffer reader;

  /** . */
  protected RenderPrintWriter writer;

  /** . */
  private final Pipe<Object> producer = new Pipe<Object>() {
    public void provide(Object element) throws IOException {
      if (producedItems.isEmpty()) {
        producedItems = new LinkedList<Object>();
      }
      producedItems.add(element);
    }
    public void flush() throws IOException {
    }
  };

  public TestInvocationContext() {
    super(new HashMap<String, Object>(), new HashMap<String, Object>());

    //
    this.reader = null;
    this.writer = null;
    this.producedItems = Collections.emptyList();
  }

  public boolean takeAlternateBuffer() {
    return false;
  }

  public boolean releaseAlternateBuffer() {
    return false;
  }

  public Class<Object> getConsumedType() {
    return Object.class;
  }

  public int getWidth() {
    return 32;
  }

  public int getHeight() {
    return 40;
  }

  public String getProperty(String propertyName) {
    return null;
  }

  public String readLine(String msg, boolean echo) {
    throw new UnsupportedOperationException();
  }

  public void provide(Object element) throws IOException {
    if (element instanceof Chunk) {
      if (reader == null) {
        reader = new ChunkBuffer();
      }
      reader.provide((Chunk)element);
    } else {
      producer.provide(element);
    }
  }

  public void flush() throws IOException {
    producer.flush();
  }

  public CommandInvoker<?, ?> resolve(String s) throws ScriptException, IOException {
    throw new UnsupportedOperationException();
  }

  public List<Object> getProducedItems() {
    return producedItems;
  }

  public ChunkBuffer getReader() {
    return reader;
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
    CommandInvoker<C, Object> invoker = (CommandInvoker<C, Object>)command.resolveInvoker(sb.toString());
    invoker.open(this);
    invoker.flush();
    invoker.close();
    return reader != null ? reader.toString() : null;
  }
}
