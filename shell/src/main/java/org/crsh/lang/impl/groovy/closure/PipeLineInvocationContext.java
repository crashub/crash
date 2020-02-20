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

package org.crsh.lang.impl.groovy.closure;

import org.crsh.command.ShellSafety;
import org.crsh.command.ShellSafetyFactory;
import org.crsh.shell.impl.command.AbstractInvocationContext;
import org.crsh.shell.impl.command.spi.CommandException;
import org.crsh.text.Screenable;
import org.crsh.shell.impl.command.spi.CommandInvoker;
import org.crsh.command.InvocationContext;
import org.crsh.text.RenderPrintWriter;
import org.crsh.text.Style;

import java.io.IOException;
import java.util.Map;

class PipeLineInvocationContext extends AbstractInvocationContext<Object> {

  /** . */
  final InvocationContext<Object> outter;

  PipeLineInvocationContext(InvocationContext<Object> outter) {

    //
    this.outter = outter;
  }

  @Override
  public ShellSafety getShellSafety() {
    return ShellSafetyFactory.getCurrentThreadShellSafety();
  }

  public CommandInvoker<?, ?> resolve(String s) throws CommandException {
    return outter.resolve(s);
  }

  public int getWidth() {
    return outter.getWidth();
  }

  public int getHeight() {
    return outter.getHeight();
  }

  public boolean takeAlternateBuffer() throws IOException {
    return outter.takeAlternateBuffer();
  }

  public boolean releaseAlternateBuffer() throws IOException {
    return outter.releaseAlternateBuffer();
  }

  public String getProperty(String propertyName) {
    return outter.getProperty(propertyName);
  }

  public String readLine(String msg, boolean echo) throws IOException, InterruptedException {
    return outter.readLine(msg, echo);
  }

  public RenderPrintWriter getWriter() {
    return outter.getWriter();
  }

  public Class<Object> getConsumedType() {
    return Object.class;
  }

  public Screenable append(CharSequence s) throws IOException {
    outter.append(s);
    return this;
  }

  public Appendable append(char c) throws IOException {
    outter.append(c);
    return this;
  }

  public Appendable append(CharSequence csq, int start, int end) throws IOException {
    outter.append(csq, start, end);
    return this;
  }

  public Screenable append(Style style) throws IOException {
    outter.append(style);
    return this;
  }

  public Screenable cls() throws IOException {
    outter.cls();
    return  this;
  }

  public void provide(Object element) throws Exception {
    outter.provide(element);
  }

  public void flush() throws IOException {
    outter.flush();
  }

  public void close() {
    // Nothing to do
  }

  public Map<String, Object> getSession() {
    return outter.getSession();
  }

  public Map<String, Object> getAttributes() {
    return outter.getAttributes();
  }
}
