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

package org.crsh.cmdline.matcher.impl;

import org.crsh.cmdline.ClassDescriptor;
import org.crsh.cmdline.CommandDescriptor;
import org.crsh.cmdline.Termination;
import org.crsh.cmdline.matcher.tokenizer.Tokenizer;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public final class Parser<T> implements Iterator<Event> {

  /** . */
  private final Tokenizer tokenizer;

  /** . */
  private final String mainName;

  /** . */
  private final Mode mode;

  /** . */
  private CommandDescriptor<T, ?> command;

  /** . */
  private Status status;

  /** . */
  private final LinkedList<Event> next;

  public Parser(Tokenizer tokenizer, ClassDescriptor<T> command, String mainName, Mode mode) {
    this.tokenizer = tokenizer;
    this.command = command;
    this.mainName = mainName;
    this.status = new Status.ReadingOption();
    this.mode = mode;
    this.next = new LinkedList<Event>();
  }

  public Mode getMode() {
    return mode;
  }

  public int getIndex() {
    return tokenizer.getIndex();
  }

  public Status getStatus() {
    return status;
  }

  public Termination getTermination() {
    return tokenizer.getTermination();
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
      Status.Response nextStatus = status.process(new Status.Request(mode, mainName, tokenizer, command));
      if (nextStatus.status != null) {
        this.status = nextStatus.status;
      }
      if (nextStatus.events != null) {
        next.addAll(nextStatus.events);
      }
      if (nextStatus.command != null) {
        command = (CommandDescriptor<T, ?>)nextStatus.command;
      }
    }
  }
}
