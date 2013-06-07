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
import org.crsh.command.ScriptException;
import org.crsh.io.Filter;
import org.crsh.shell.ScreenContext;
import org.crsh.text.Chunk;
import org.crsh.text.ChunkAdapter;
import org.crsh.util.Safe;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
abstract class PipeFilter<C, P> implements Filter<C, P, CommandContext<P>>, CommandContext<C> {

  /** . */
  protected CommandContext<P> context;

  /** . */
  protected final boolean piped;

  protected PipeFilter(boolean piped) {
    this.piped = piped;
  }

  public final boolean isPiped() {
    return piped;
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

  static class Noop<P> extends PipeFilter<P, P> {

    Noop(boolean piped) {
      super(piped);
    }

    public void provide(P element) throws IOException {
      context.provide(element);
    }

    public Class<P> getConsumedType() {
      return context.getConsumedType();
    }

    public void flush() throws IOException {
      context.flush();
    }

    public Class<P> getProducedType() {
      return context.getConsumedType();
    }

    public void open(CommandContext<P> consumer) {
      context = consumer;
    }

    public void close() {
      Safe.close(context);
    }
  }

  static class Chunkizer extends PipeFilter<Object, Chunk> {

    /** . */
    private ChunkAdapter ca;

    Chunkizer(boolean piped) {
      super(piped);
    }

    public Class<Chunk> getProducedType() {
      return Chunk.class;
    }

    public Class<Object> getConsumedType() {
      return Object.class;
    }

    public void open(final CommandContext<Chunk> consumer) {
      ca = new ChunkAdapter(new ScreenContext<Chunk>() {
        public int getWidth() {
          return consumer.getWidth();
        }
        public int getHeight() {
          return consumer.getHeight();
        }
        public Class<Chunk> getConsumedType() {
          return Chunk.class;
        }
        public void provide(Chunk element) throws IOException {
          Chunkizer.this.context.provide(element);
        }
        public void flush() throws IOException {
          Chunkizer.this.context.flush();
        }
      });

      this.context = consumer;
    }

    public void provide(Object element) throws ScriptException, IOException {
      ca.provide(element);
    }

    public void flush() throws ScriptException, IOException {
      ca.flush();
    }

    public void close() throws ScriptException, IOException {
      context.close();
    }
  }

  static class Sink<P> extends PipeFilter<Object, P> {

    /** . */
    private final Class<P> producedType;

    Sink(Class<P> producedType, boolean piped) {
      super(piped);

      //
      this.producedType = producedType;
    }

    public Class<P> getProducedType() {
      return producedType;
    }

    public void open(CommandContext<P> consumer) {
      this.context = consumer;
    }

    public void provide(Object element) throws IOException {
    }

    public Class<Object> getConsumedType() {
      return Object.class;
    }

    public void flush() throws IOException {
      context.flush();
    }

    public void close() throws IOException {
      context.close();
    }
  }
}
