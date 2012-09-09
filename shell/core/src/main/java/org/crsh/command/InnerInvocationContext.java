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

import org.crsh.text.ShellPrintWriter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

class InnerInvocationContext<P> implements InvocationContext<Void, P> {

  /** . */
  final InvocationContext<?, ?> outter;

  /** . */
  final Class<? extends P> producedType;

  /** . */
  List<P> products;

  /** . */
  final boolean piped;

  InnerInvocationContext(
    InvocationContext<?, ?> outter,
    Class<? extends P> producedType,
    boolean piped) {
    this.outter = outter;
    this.products = Collections.emptyList();
    this.producedType = producedType;
    this.piped = piped;
  }

  public int getWidth() {
    return outter.getWidth();
  }

  public String getProperty(String propertyName) {
    return outter.getProperty(propertyName);
  }

  public String readLine(String msg, boolean echo) {
    return outter.readLine(msg, echo);
  }

  public ShellPrintWriter getWriter() {
    return outter.getWriter();
  }

  public boolean isPiped() {
    return piped;
  }

  public Iterable<Void> consume() throws IllegalStateException {
    throw new IllegalStateException();
  }

  public void produce(P product) {
    if (products.isEmpty()) {
      products = new ArrayList<P>();
    }
    products.add(product);
  }

  public Map<String, Object> getSession() {
    return outter.getSession();
  }

  public Map<String, Object> getAttributes() {
    return outter.getAttributes();
  }
}
