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

package org.crsh.cli.impl.tokenizer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class Tokenizer implements Iterator<Token> {

  /** . */
  private ArrayList<Token> stack;

  /** . */
  private int ptr;

  /** . */
  private int index = 0;

  protected Tokenizer() {
    this.stack = new ArrayList<Token>();
    this.ptr = 0;
  }

  public final boolean hasNext() {
    if (ptr < stack.size()) {
      return true;
    } else {
      Token next = parse();
      if (next != null) {
        stack.add(next);
      }
      return next != null;
    }
  }

  public final void pushBack(int count) {
    if (count < 0) {
      throw new IllegalArgumentException();
    }
    if (ptr - count < 0) {
      throw new IllegalStateException("Trying to push back too many tokens");
    } else {
      while (count > 0) {
        index -= stack.get(--ptr).raw.length();
        count--;
      }
    }
  }

  public final Token peek() {
    if (hasNext()) {
      return stack.get(ptr);
    } else {
      return null;
    }
  }

  public final Token next() {
    if (hasNext()) {
      Token token = stack.get(ptr++);
      index += token.raw.length();
      return token;
    } else {
      throw new NoSuchElementException();
    }
  }

  public final void remove() {
    throw new UnsupportedOperationException();
  }

  public final void pushBack() {
    pushBack(1);
  }

  protected abstract Token parse();

  public final int getIndex() {
    return index;
  }

}