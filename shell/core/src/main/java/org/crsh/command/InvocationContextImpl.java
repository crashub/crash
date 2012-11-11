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

import org.crsh.io.ScreenContext;
import org.crsh.io.ProducerContext;
import org.crsh.shell.impl.command.CRaSHSession;
import org.crsh.shell.impl.command.PipeLineFactory;
import org.crsh.shell.impl.command.PipeLineParser;
import org.crsh.text.Chunk;
import org.crsh.text.RenderPrintWriter;

import java.io.IOException;
import java.util.Map;

final class InvocationContextImpl<P> implements InvocationContext<P> {

  /** . */
  private final ProducerContext<P> producerContext;

  /** . */
  private final CommandContext sessionContext;

  /** . */
  private RenderPrintWriter writer;

  InvocationContextImpl(ProducerContext<P> producerContext, CommandContext sessionContext) {
    this.producerContext = producerContext;
    this.sessionContext = sessionContext;
  }

  public RenderPrintWriter getWriter() {
    if (writer == null) {
      writer = new RenderPrintWriter(new ScreenContext<Chunk>() {
        public int getWidth() {
          return producerContext.getWidth();
        }
        public int getHeight() {
          return producerContext.getHeight();
        }
        public void provide(Chunk element) throws IOException {
          Class<P> consumedType = producerContext.getConsumedType();
          if (consumedType.isInstance(element)) {
            P p = consumedType.cast(element);
            producerContext.provide(p);
          }
        }
        public void flush() throws IOException {
          producerContext.flush();
        }
      });
    }
    return writer;
  }

  public boolean takeAlternateBuffer() {
    return producerContext.takeAlternateBuffer();
  }

  public boolean releaseAlternateBuffer() {
    return producerContext.releaseAlternateBuffer();
  }

  public CommandInvoker<?, ?> resolve(String s) throws ScriptException, IOException {
    // A bit nasty : will improve that later
    CRaSHSession session = (CRaSHSession)getSession();
    PipeLineParser parser= new PipeLineParser(s);
    PipeLineFactory factory = parser.parse();
    try {
      return factory.create(session);
    }
    catch (NoSuchCommandException e) {
      throw new ScriptException(e);
    }
  }

  public Class<P> getConsumedType() {
    return producerContext.getConsumedType();
  }

  public String getProperty(String propertyName) {
    return producerContext.getProperty(propertyName);
  }

  public String readLine(String msg, boolean echo) {
    return producerContext.readLine(msg, echo);
  }

  public int getWidth() {
    return producerContext.getWidth();
  }

  public int getHeight() {
    return producerContext.getHeight();
  }

  public void provide(P element) throws IOException {
    producerContext.provide(element);
  }

  public void flush() throws IOException {
    producerContext.flush();
  }

  public Map<String, Object> getSession() {
    return sessionContext.getSession();
  }

  public Map<String, Object> getAttributes() {
    return sessionContext.getAttributes();
  }

  public InvocationContextImpl<P> leftShift(Object o) throws IOException {
    if (producerContext.getConsumedType().isInstance(o)) {
      P p = producerContext.getConsumedType().cast(o);
      producerContext.provide(p);
    }
    return this;
  }
}
