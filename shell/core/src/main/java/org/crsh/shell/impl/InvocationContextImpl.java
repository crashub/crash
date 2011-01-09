/*
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.crsh.shell.impl;

import org.crsh.command.InvocationContext;
import org.crsh.shell.ShellProcessContext;
import org.crsh.shell.io.ShellPrinter;
import org.crsh.util.LineFeedWriter;

import java.io.StringWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class InvocationContextImpl<C, P> extends CommandContextImpl implements InvocationContext<C, P> {

  /** . */
  private final ShellProcessContext processContext;

  /** . */
  private ShellPrinter writer;

  /** . */
  private StringWriter buffer;

  /** . */
  private List<P> products;

  /** . */
  private Iterable<C> consumedItems;

  InvocationContextImpl(
    ShellProcessContext processContext,
    Iterable<C> consumedItems,
    Map<String, Object> attributes) {
    super(attributes);
    this.processContext = processContext;
    this.writer = null;
    this.buffer = null;
    this.consumedItems = consumedItems;
    this.products = Collections.emptyList();
  }

  public int getWidth() {
    return processContext.getWidth();
  }

  public List<P> getProducedItems() {
    return products;
  }

  public StringWriter getBuffer() {
    return buffer;
  }

  public boolean isPiped() {
    return consumedItems != null;
  }

  public Iterable<C> consume() {
    if (consumedItems == null) {
      throw new IllegalStateException("Cannot consume as no pipe operation is involved");
    }
    return consumedItems;
  }

  public void produce(P product) {
    if (products.isEmpty()) {
      products = new LinkedList<P>();
    }
    products.add(product);
  }

  public ShellPrinter getWriter() {
    if (writer == null) {
      buffer = new StringWriter();
      writer = new ShellPrinter(new LineFeedWriter(buffer, "\r\n"));
    }
    return writer;
  }
  public String readLine(String msg, boolean echo) {
    return processContext.readLine(msg, echo);
  }
}
