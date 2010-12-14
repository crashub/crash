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

import org.crsh.command.CommandContext;
import org.crsh.shell.ShellProcessContext;
import org.crsh.shell.io.ShellPrinter;
import org.crsh.util.LineFeedWriter;

import java.io.StringWriter;
import java.util.*;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class CommandContextImpl<C, P> implements CommandContext<C, P> {

  /** . */
  private final ShellProcessContext responseContext;

  /** . */
  private final Map<String, Object> attributes;

  /** . */
  private ShellPrinter writer;

  /** . */
  private StringWriter buffer;

  /** . */
  private List<P> products;

  /** . */
  private Iterable<C> consumedItems;

  CommandContextImpl(
      ShellProcessContext responseContext,
      Iterable<C> consumedItems,
      Map<String, Object> attributes) {
    this.attributes = attributes;
    this.responseContext = responseContext;
    this.writer = null;
    this.buffer = null;
    this.consumedItems = consumedItems;
    this.products = Collections.emptyList();
  }

  public List<P> getProducedItems() {
    return products;
  }

  public StringWriter getBuffer() {
    return buffer;
  }

  public Map<String, Object> getAttributes() {
    return attributes;
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
    return responseContext.readLine(msg, echo);
  }
}
