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

package org.crsh.util;

import java.util.concurrent.Callable;

/**
 * An helper for chaining statements to execute.
 */
public abstract class Statement {

  /** . */
  private Statement next;

  protected abstract void run() throws Throwable;

  public Statement with(final Runnable runnable) {
    return with(new Statement() {
      @Override
      protected void run() throws Throwable {
        runnable.run();
      }
    });
  }

  public Statement with(final Callable<?> callable) {
    return with(new Statement() {
      @Override
      protected void run() throws Throwable {
        callable.call();
      }
    });
  }

  public Statement with(Statement callback) {
    if (next != null) {
      next.with(callback);
    } else {
      next = callback;
    }
    return this;
  }

  public void all() {
    try {
      run();
    }
    catch (Throwable ignore) {
    }
    if (next != null) {
      next.all();
    }
  }
}
