/*
 * Copyright (C) 2011 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 *
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

package org.crsh.command.impl;

import org.crsh.command.InvocationContext;
import org.crsh.shell.io.ShellPrinter;
import org.crsh.shell.io.ShellWriter;
import org.crsh.text.CharReader;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class BaseInvocationContext<C, P> extends BaseCommandContext implements InvocationContext<C, P> {

  /** . */
  protected ShellPrinter writer;

  /** . */
  protected CharReader reader;

  /** . */
  protected List<P> producedItems;

  /** . */
  protected Iterable<C> consumedItems;

  protected BaseInvocationContext(
    Iterable<C> consumedItems,
    Map<String, Object> session,
    Map<String, Object> attributes) {
    super(session, attributes);

    //
    this.writer = null;
    this.reader = null;
    this.consumedItems = consumedItems;
    this.producedItems = Collections.emptyList();
  }

  public List<P> getProducedItems() {
    return producedItems;
  }

  public CharReader getReader() {
    return reader;
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
    if (producedItems.isEmpty()) {
      producedItems = new LinkedList<P>();
    }
    producedItems.add(product);
  }

  public ShellPrinter getWriter() {
    if (writer == null) {
      reader = new CharReader();
      writer = new ShellPrinter(new ShellWriter(reader, "\r\n"), this);
    }
    return writer;
  }
}
