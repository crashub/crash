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

package org.crsh.shell.impl.command;

import org.crsh.command.InvocationContext;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class BaseInvocationContext<C, P> extends BaseCommandContext implements InvocationContext<C, P> {

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
    this.consumedItems = consumedItems;
    this.producedItems = Collections.emptyList();
  }

  public List<P> getProducedItems() {
    return producedItems;
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
}
