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
package org.crsh.shell.impl.command.pipeline;

import org.crsh.command.CommandContext;
import org.crsh.io.Consumer;
import org.crsh.io.Producer;
import org.crsh.text.ScreenAppendable;
import org.crsh.text.ScreenContext;
import org.crsh.shell.impl.command.spi.CommandInvoker;
import org.crsh.text.CLS;
import org.crsh.text.ScreenContextConsumer;
import org.crsh.text.Style;

import java.io.IOException;
import java.util.Map;

/** @author Julien Viet */
class CommandInvokerAdapter<C2, C extends C2, P, CONSUMER extends CommandContext<? super P>>
    implements Consumer<C2>, Producer<P, CONSUMER>, CommandContext<C2> {

  /** . */
  final CommandInvoker<C, P> command;

  /** . */
  protected CONSUMER consumer;

  /** . */
  private final Class<C> consumedType;

  /** . */
  private final Class<P> producedType;

  /** . */
  private final Class<C2> fooClass;

  /** . */
  private ScreenContextConsumer adapter;

  /** . */
  private ScreenContext screenContext;

  CommandInvokerAdapter(CommandInvoker<C, P> command, Class<C> consumedType, Class<P> producedType, Class<C2> fooClass) {
    this.consumedType = consumedType;
    this.producedType = producedType;
    this.fooClass = fooClass;
    this.consumer = null;
    this.command = command;
    this.screenContext = null;
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

  public String readLine(String msg, boolean echo) throws IOException, InterruptedException {
    return consumer.readLine(msg, echo);
  }

  public Map<String, Object> getSession() {
    return consumer.getSession();
  }

  public Map<String, Object> getAttributes() {
    return consumer.getAttributes();
  }

  public int getWidth() {
    return screenContext.getWidth();
  }

  public int getHeight() {
    return screenContext.getHeight();
  }

  public void open(CONSUMER consumer) {

    //
    command.open(consumer);

    //
    ScreenContext screenContext = command.getScreenContext();
    if (screenContext == null) {
      screenContext = consumer;
    }

    //
    this.screenContext = screenContext;
    this.adapter = new ScreenContextConsumer(screenContext);
    this.consumer = consumer;
  }

  @Override
  public Class<C2> getConsumedType() {
    return fooClass;
  }

  @Override
  public Class<P> getProducedType() {
    return producedType;
  }

  @Override
  public void provide(Object element) throws IOException {
    if (consumedType.equals(Void.class)) {
      // Discard
    } else {
      if (consumedType.isInstance(element)) {
        C c = consumedType.cast(element);
        command.provide(c);
      } else {
        adapter.provide(element);
      }
    }
  }

  @Override
  public Appendable append(char c) throws IOException {
    if (consumedType.equals(Void.class)) {
      // Discard
    } else {
      if (CharSequence.class.isAssignableFrom(consumedType)) {
        provide(consumedType.cast(Character.toString(c)));
      } else {
        screenContext.append(c);
      }
    }
    return this;
  }

  @Override
  public Appendable append(CharSequence s) throws IOException {
    if (consumedType.equals(Void.class)) {
      // Discard
    } else {
      if (CharSequence.class.isAssignableFrom(consumedType)) {
        provide(consumedType.cast(s));
      } else {
        screenContext.append(s);
      }
    }
    return this;
  }

  @Override
  public Appendable append(CharSequence csq, int start, int end) throws IOException {
    if (consumedType.equals(Void.class)) {
      // Discard
    } else {
      if (CharSequence.class.isAssignableFrom(consumedType)) {
        provide(consumedType.cast(csq.subSequence(start, end)));
      } else {
        screenContext.append(csq, start, end);
      }
    }
    return this;
  }

  @Override
  public ScreenAppendable append(Style style) throws IOException {
    if (consumedType.equals(Void.class)) {
      // Discard
    } else {
      if (Style.class.isAssignableFrom(consumedType)) {
        provide(consumedType.cast(style));
      } else {
        screenContext.append(style);
      }
    }
    return this;
  }

  @Override
  public ScreenAppendable cls() throws IOException {
    if (consumedType.equals(Void.class)) {
      // Discard
    } else {
      if (CLS.class.isAssignableFrom(consumedType)) {
        provide(consumedType.cast(CLS.INSTANCE));
      } else {
        screenContext.cls();
      }
    }
    return this;
  }

  public void flush() throws IOException {
    adapter.flush();
    command.flush();
  }

  public void close() throws IOException {
    adapter.flush();
    command.close();
  }
}
