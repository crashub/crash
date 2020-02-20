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

package test.command;

import org.crsh.cli.impl.descriptor.IntrospectionException;
import org.crsh.command.*;
import org.crsh.shell.impl.command.spi.CommandException;
import org.crsh.lang.impl.java.ClassShellCommand;
import org.crsh.shell.impl.command.RuntimeContextImpl;
import org.crsh.shell.impl.command.spi.Command;
import org.crsh.shell.impl.command.spi.CommandInvoker;
import org.crsh.lang.impl.groovy.command.GroovyScriptCommand;
import org.crsh.lang.impl.groovy.command.GroovyScriptShellCommand;
import org.crsh.text.CLS;
import org.crsh.text.Screenable;
import org.crsh.text.ScreenBuffer;
import org.crsh.text.Style;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.*;

public class TestInvocationContext<C> extends RuntimeContextImpl implements CommandContext<Object> {

  /** . */
  protected List<Object> producedItems;

  /** . */
  protected ScreenBuffer reader;

  public TestInvocationContext() {
    super(new HashMap<String, Object>(), new HashMap<String, Object>());

    //
    this.reader = null;
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

  public Screenable append(CharSequence s) throws IOException {
    if (reader == null) {
      reader = new ScreenBuffer();
    }
    reader.append(s);
    return this;
  }

  public Screenable append(char c) throws IOException {
    if (reader == null) {
      reader = new ScreenBuffer();
    }
    reader.append(c);
    return this;
  }

  public Screenable append(CharSequence csq, int start, int end) throws IOException {
    if (reader == null) {
      reader = new ScreenBuffer();
    }
    reader.append(csq, start, end);
    return this;
  }

  public Screenable append(Style style) throws IOException {
    if (reader == null) {
      reader = new ScreenBuffer();
    }
    reader.append(style);
    return this;
  }

  public Screenable cls() throws IOException {
    if (reader == null) {
      reader = new ScreenBuffer();
    }
    reader.cls();
    return this;
  }

  public void provide(Object element) throws IOException {
    if (element instanceof Style || element instanceof CLS) {
      if (reader == null) {
        reader = new ScreenBuffer();
      }
      reader.append(element);
    } else {
      if (producedItems.isEmpty()) {
        producedItems = new LinkedList<Object>();
      }
      producedItems.add(element);
    }
  }

  public void flush() throws IOException {
  }

  public void close() throws IOException {
  }

  public CommandInvoker<?, ?> resolve(String s) throws ScriptException, IOException {
    throw new UnsupportedOperationException();
  }

  public List<Object> getProducedItems() {
    return producedItems;
  }

  public ScreenBuffer getReader() {
    return reader;
  }

  public <B extends BaseCommand> String execute(Class<B> commandClass, String... args) throws IntrospectionException, IOException, CommandException  {
    return execute(new ClassShellCommand<B>(commandClass, ShellSafetyFactory.getCurrentThreadShellSafety()), args);
  }

  public <B extends GroovyScriptCommand> String execute2(Class<B> commandClass, String... args) throws IntrospectionException, IOException, UndeclaredThrowableException, CommandException {
    return execute(new GroovyScriptShellCommand<B>(commandClass), args);
  }

  private String execute(Command<?> command, String... args) throws IOException, UndeclaredThrowableException, CommandException {
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
