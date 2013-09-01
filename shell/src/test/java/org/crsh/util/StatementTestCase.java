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

import junit.framework.TestCase;

import java.util.concurrent.atomic.AtomicBoolean;

public class StatementTestCase extends TestCase {

  public void testInvoke() {
    final AtomicBoolean done = new AtomicBoolean(false);
    new Statement() {
      @Override
      protected void run() throws Throwable {
        done.set(true);
      }
    }.all();
    assertTrue(done.get());
  }

  public void testInvokeNextWithCheckedException() {
    final AtomicBoolean done = new AtomicBoolean(false);
    new Statement() {
      @Override
      protected void run() throws Throwable {
        throw new Exception();
      }
    }.with(new Statement() {
      @Override
      protected void run() throws Throwable {
        done.set(true);
      }
    }).all();
    assertTrue(done.get());
  }

  public void testInvokeWithRuntimeException() {
    final AtomicBoolean done = new AtomicBoolean(false);
    new Statement() {
      @Override
      protected void run() throws Throwable {
        throw new Exception();
      }
    }.with(new Statement() {
      @Override
      protected void run() throws Throwable {
        done.set(true);
      }
    }).all();
    assertTrue(done.get());
  }

  public void testInvokeWithError() {
    final AtomicBoolean done = new AtomicBoolean(false);
    new Statement() {
      @Override
      protected void run() throws Throwable {
        throw new Error();
      }
    }.with(new Statement() {
      @Override
      protected void run() throws Throwable {
        done.set(true);
      }
    }).all();
    assertTrue(done.get());
  }

  public void testInvokeWithThrowable() {
    final AtomicBoolean done = new AtomicBoolean(false);
    new Statement() {
      @Override
      protected void run() throws Throwable {
        throw new Throwable();
      }
    }.with(new Statement() {
      @Override
      protected void run() throws Throwable {
        done.set(true);
      }
    }).all();
    assertTrue(done.get());
  }
}
