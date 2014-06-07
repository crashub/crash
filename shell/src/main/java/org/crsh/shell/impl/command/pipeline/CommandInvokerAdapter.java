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
import org.crsh.shell.impl.command.spi.CommandException;
import org.crsh.stream.Consumer;
import org.crsh.stream.Producer;
import org.crsh.text.Screenable;
import org.crsh.text.ScreenContext;
import org.crsh.shell.impl.command.spi.CommandInvoker;
import org.crsh.text.CLS;
import org.crsh.text.ScreenContextConsumer;
import org.crsh.text.Style;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Map;

/** @author Julien Viet */
class CommandInvokerAdapter<C, P, CONSUMER extends CommandContext<? super P>>
    implements Consumer<Object>, Producer<P, CONSUMER>, CommandContext<Object> {

  /** . */
  final CommandInvoker<C, P> command;

  /** . */
  protected CONSUMER consumer;

  /** . */
  private final Class<C> consumedType;

  /** . */
  private final Class<P> producedType;

  /** . */
  private ScreenContextConsumer adapter;

  /** . */
  private ScreenContext screenContext;

  /** . */
  private final boolean charSequenceConsumer;

  /** . */
  private final boolean styleConsumer;

  /** . */
  private final boolean clsConsumer;

  CommandInvokerAdapter(CommandInvoker<C, P> command, Class<C> consumedType, Class<P> producedType) {
    this.consumedType = consumedType;
    this.producedType = producedType;
    this.consumer = null;
    this.command = command;
    this.screenContext = null;
    this.charSequenceConsumer = consumedType.isAssignableFrom(CharSequence.class);
    this.styleConsumer = consumedType.isAssignableFrom(Style.class);
    this.clsConsumer = consumedType.isAssignableFrom(CLS.class);
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
    return screenContext != null ? screenContext.getWidth() : consumer.getWidth();
  }

  public int getHeight() {
    return screenContext != null ? screenContext.getHeight() : consumer.getHeight();
  }

  public void open(final CONSUMER consumer) throws Exception {

    //
    try {
      command.open(consumer);
    }
    catch (CommandException e) {
      Throwable cause = e.getCause();
      if (cause instanceof Exception) {
        throw (Exception)cause;
      } else if (cause instanceof Error) {
        throw (Error)cause;
      } else {
        throw new UndeclaredThrowableException(cause);
      }
    }

    //
    ScreenContext screenContext = command.getScreenContext();
    ScreenContextConsumer adapter = screenContext != null ? new ScreenContextConsumer(screenContext) : null;

    //
    this.screenContext = screenContext;
    this.adapter = adapter;
    this.consumer = consumer;
  }

  @Override
  public Class<Object> getConsumedType() {
    return Object.class;
  }

  @Override
  public Class<P> getProducedType() {
    return producedType;
  }

  @Override
  public void provide(Object element) throws Exception  {
    if (adapter != null) {
      adapter.provide(element);
    }
    if (consumedType.isInstance(element)) {
      try {
        command.provide(consumedType.cast(element));
      }
      catch (CommandException e) {
        Throwable cause = e.getCause();
        if (cause instanceof Exception) {
          throw (Exception)cause;
        } else if (cause instanceof Error) {
          throw (Error)cause;
        } else {
          throw new UndeclaredThrowableException(cause);
        }
      }
    }
  }

  @Override
  public Appendable append(char c) throws IOException {
    if (screenContext != null) {
      screenContext.append(c);
    }
    if (charSequenceConsumer) {
      try {
        command.provide(consumedType.cast(Character.toString(c)));
      }
      catch (CommandException e) {
        Throwable cause = e.getCause();
        if (cause instanceof RuntimeException) {
          throw (RuntimeException)cause;
        } else if (cause instanceof Error) {
          throw (Error)cause;
        } else {
          throw new UndeclaredThrowableException(cause);
        }
      }
    }
    return this;
  }

  @Override
  public Appendable append(CharSequence s) throws IOException {
    if (screenContext != null) {
      screenContext.append(s);
    }
    if (charSequenceConsumer) {
      try {
        command.provide(consumedType.cast(s));
      }
      catch (CommandException e) {
        Throwable cause = e.getCause();
        if (cause instanceof RuntimeException) {
          throw (RuntimeException)cause;
        } else if (cause instanceof Error) {
          throw (Error)cause;
        } else {
          throw new UndeclaredThrowableException(cause);
        }
      }
    }
    return this;
  }

  @Override
  public Appendable append(CharSequence csq, int start, int end) throws IOException {
    if (screenContext != null) {
      screenContext.append(csq, start, end);
    }
    if (charSequenceConsumer) {
      try {
        command.provide(consumedType.cast(csq.subSequence(start, end)));
      }
      catch (CommandException e) {
        Throwable cause = e.getCause();
        if (cause instanceof RuntimeException) {
          throw (RuntimeException)cause;
        } else if (cause instanceof Error) {
          throw (Error)cause;
        } else {
          throw new UndeclaredThrowableException(cause);
        }
      }
    }
    return this;
  }

  @Override
  public Screenable append(Style style) throws IOException {
    if (screenContext != null) {
      screenContext.append(style);
    }
    if (styleConsumer) {
      try {
        command.provide(consumedType.cast(style));
      }
      catch (CommandException e) {
        Throwable cause = e.getCause();
        if (cause instanceof RuntimeException) {
          throw (RuntimeException)cause;
        } else if (cause instanceof Error) {
          throw (Error)cause;
        } else {
          throw new UndeclaredThrowableException(cause);
        }
      }
    }
    return this;
  }

  @Override
  public Screenable cls() throws IOException {
    if (screenContext != null) {
      screenContext.cls();
    }
    if (clsConsumer) {
      try {
        command.provide(consumedType.cast(CLS.INSTANCE));
      }
      catch (CommandException e) {
        Throwable cause = e.getCause();
        if (cause instanceof RuntimeException) {
          throw (RuntimeException)cause;
        } else if (cause instanceof Error) {
          throw (Error)cause;
        } else {
          throw new UndeclaredThrowableException(cause);
        }
      }
    }
    return this;
  }

  public void flush() throws IOException {
    if (adapter != null) {
      adapter.flush();
    }
    command.flush();
  }

  public void close() throws Exception {
    if (adapter != null) {
      adapter.flush();
    }
    try {
      command.close();
    }
    catch (CommandException e) {
      Throwable cause = e.getCause();
      if (cause instanceof Exception) {
        throw (Exception)cause;
      } else if (cause instanceof Error) {
        throw (Error)cause;
      } else {
        throw new UndeclaredThrowableException(cause);
      }
    }
  }
}
