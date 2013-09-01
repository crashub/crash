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
package org.crsh.command.pipeline;

import org.crsh.command.CommandContext;
import org.crsh.io.Consumer;
import org.crsh.io.Producer;

import java.io.IOException;
import java.util.Map;

/** @author Julien Viet */
public abstract class AbstractPipe<C, P, CONS extends CommandContext<? super P>> implements
    Consumer<C>, Producer<P, CONS>,
    CommandContext<C> {

  /** . */
  protected final boolean piped;

  /** . */
  protected CONS consumer;

  public AbstractPipe(boolean piped) {
    this.piped = piped;
    this.consumer = null;
  }

  public boolean isPiped() {
    return piped;
  }

  public boolean takeAlternateBuffer() throws IOException {
    return consumer.takeAlternateBuffer();
  }

  public boolean releaseAlternateBuffer() throws IOException {
    return consumer.releaseAlternateBuffer();
  }

  public String getProperty(String propertyName) {
    return consumer.getProperty(propertyName);
  }

  public String readLine(String msg, boolean echo) {
    return consumer.readLine(msg, echo);
  }

  public Map<String, Object> getSession() {
    return consumer.getSession();
  }

  public Map<String, Object> getAttributes() {
    return consumer.getAttributes();
  }

  public int getWidth() {
    return consumer.getWidth();
  }

  public int getHeight() {
    return consumer.getHeight();
  }

  public void open(CONS consumer) {
    this.consumer = consumer;
  }

  public void flush() throws IOException {
    consumer.flush();
  }

  public void close() throws IOException {
    consumer.close();
  }
}
