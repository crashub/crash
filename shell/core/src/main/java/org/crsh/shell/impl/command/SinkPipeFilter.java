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

import org.crsh.io.Filter;
import org.crsh.io.ProducerContext;

import java.io.IOException;

class SinkPipeFilter<P> implements Filter<Object, P> {

  /** . */
  private Filter<P, ?> context;

  /** . */
  private final Class<P> producedType;

  SinkPipeFilter(Class<P> producedType) {
    this.producedType = producedType;
  }

  public Class<P> getProducedType() {
    return producedType;
  }

  public boolean takeAlternateBuffer() throws IOException {
    return context.takeAlternateBuffer();
  }

  public boolean releaseAlternateBuffer() throws IOException {
    return context.releaseAlternateBuffer();
  }

  public void setPiped(boolean piped) {
  }

  public void open(ProducerContext<P> context) {
    this.context = (Filter<P, ?>)context;
  }

  public void close() {
    context.close();
  }

  public String getProperty(String propertyName) {
    return context.getProperty(propertyName);
  }

  public String readLine(String msg, boolean echo) {
    return context.readLine(msg, echo);
  }

  public int getWidth() {
    return context.getWidth();
  }

  public int getHeight() {
    return context.getHeight();
  }

  public void provide(Object element) throws IOException {
  }

  public Class<Object> getConsumedType() {
    return Object.class;
  }

  public void flush() throws IOException {
    context.flush();
  }
}
