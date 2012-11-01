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

import org.crsh.io.Consumer;
import org.crsh.text.Chunk;
import org.crsh.io.ScreenContext;
import org.crsh.text.RenderPrintWriter;

import java.io.IOException;
import java.util.Map;

class InnerInvocationContext<P> implements InvocationContext<P> {

  /** . */
  final InvocationContext<?> outter;

  /** . */
  final Consumer<P> consumer;

  /** . */
  private RenderPrintWriter writer;

  InnerInvocationContext(
    InvocationContext<?> outter,
    Consumer<P> consumer) {

    //
    this.outter = outter;
    this.consumer = consumer;
  }

  public CommandInvoker<?, ?> resolve(String s) throws ScriptException, IOException {
    return outter.resolve(s);
  }

  public int getWidth() {
    return outter.getWidth();
  }

  public int getHeight() {
    return outter.getHeight();
  }

  public boolean takeAlternateBuffer() {
    return outter.takeAlternateBuffer();
  }

  public boolean releaseAlternateBuffer() {
    return outter.releaseAlternateBuffer();
  }

  public String getProperty(String propertyName) {
    return outter.getProperty(propertyName);
  }

  public String readLine(String msg, boolean echo) {
    return outter.readLine(msg, echo);
  }

  public RenderPrintWriter getWriter() {
    if (writer == null) {
      writer = new RenderPrintWriter(new ScreenContext<Chunk>() {
        public int getWidth() {
          return outter.getWidth();
        }
        public int getHeight() {
          return outter.getHeight();
        }
        public void provide(Chunk element) throws IOException {
          Class<P> consumedType = consumer.getConsumedType();
          if (consumedType.isInstance(element)) {
            P p = consumedType.cast(element);
            consumer.provide(p);
          }
        }
        public void flush() throws IOException {
          consumer.flush();
        }
      });
    }
    return writer;
  }

  public Class<P> getConsumedType() {
    return consumer.getConsumedType();
  }

  public void provide(P element) throws IOException {
    consumer.provide(element);
  }

  public void flush() throws IOException {
    consumer.flush();
  }

  public Map<String, Object> getSession() {
    return outter.getSession();
  }

  public Map<String, Object> getAttributes() {
    return outter.getAttributes();
  }
}
