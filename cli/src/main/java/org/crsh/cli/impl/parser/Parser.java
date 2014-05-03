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

package org.crsh.cli.impl.parser;

import org.crsh.cli.descriptor.CommandDescriptor;
import org.crsh.cli.impl.tokenizer.Tokenizer;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

public final class Parser<T> implements Iterator<Event> {

  /** . */
  private final Tokenizer tokenizer;

  /** . */
  private final Mode mode;

  /** . */
  private CommandDescriptor<T> command;

  /** . */
  private Status status;

  /** . */
  private final LinkedList<Event> next;

  public Parser(Tokenizer tokenizer, CommandDescriptor<T> command, Mode mode) {
    this.tokenizer = tokenizer;
    this.command = command;
    this.status = new Status.ReadingOption();
    this.mode = mode;
    this.next = new LinkedList<Event>();
  }

  Status getStatus() {
    return status;
  }

  public boolean hasNext() {
    if (next.isEmpty()) {
      determine();
    }
    return next.size() > 0;
  }

  public Event next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }
    return next.removeFirst();
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }

  private void determine() {
    while (next.isEmpty()) {
      Status.Response<T> nextStatus = status.process(new Status.Request<T>(mode, tokenizer, command));
      if (nextStatus.status != null) {
        this.status = nextStatus.status;
      }
      if (nextStatus.events != null) {
        next.addAll(nextStatus.events);
      }
      if (nextStatus.command != null) {
        command = (CommandDescriptor<T>)nextStatus.command;
      }
    }
  }
}
