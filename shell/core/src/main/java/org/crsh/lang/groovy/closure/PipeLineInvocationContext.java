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

package org.crsh.lang.groovy.closure;

import org.crsh.command.CommandInvoker;
import org.crsh.command.InvocationContext;
import org.crsh.command.ScriptException;
import org.crsh.text.Chunk;
import org.crsh.text.RenderPrintWriter;

import java.io.IOException;
import java.util.Map;

class PipeLineInvocationContext implements InvocationContext<Object> {

  /** . */
  final InvocationContext<Object> outter;

  /** . */
  private final boolean piped;

  PipeLineInvocationContext(
      InvocationContext<Object> outter,
      boolean piped) {

    //
    this.outter = outter;
    this.piped = piped;
  }

  public boolean isPiped() {
    return piped;
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

  public boolean takeAlternateBuffer() throws IOException {
    return outter.takeAlternateBuffer();
  }

  public boolean releaseAlternateBuffer() throws IOException {
    return outter.releaseAlternateBuffer();
  }

  public String getProperty(String propertyName) {
    return outter.getProperty(propertyName);
  }

  public String readLine(String msg, boolean echo) {
    return outter.readLine(msg, echo);
  }

  public RenderPrintWriter getWriter() {
    return outter.getWriter();
  }

  public Class<Object> getConsumedType() {
    return Object.class;
  }

  public void write(Chunk chunk) throws IOException {
    outter.write(chunk);
  }

  public void provide(Object element) throws IOException {
    outter.provide(element);
  }

  public void flush() throws IOException {
    outter.flush();
  }

  public void close() throws IOException {
    // Nothing to do
  }

  public Map<String, Object> getSession() {
    return outter.getSession();
  }

  public Map<String, Object> getAttributes() {
    return outter.getAttributes();
  }
}
