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

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class SafeTestCase extends TestCase {

  public void testClose() {
    Exception closed = Utils.close(new Closeable() {
      public void close() throws IOException {
      }
    });
    assertNull(closed);
    final IOException ioe = new IOException();
    closed = Utils.close(new Closeable() {
      public void close() throws IOException {
        throw ioe;
      }
    });
    assertSame(ioe, closed);
    final RuntimeException re = new RuntimeException();
    closed = Utils.close(new Closeable() {
      public void close() throws IOException {
        throw re;
      }
    });
    assertSame(re, closed);
    final Error thrown = new Error();
    try {
      Utils.close(new Closeable() {
        public void close() throws IOException {
          throw thrown;
        }
      });
    }
    catch (Error caught) {
      assertSame(thrown, caught);
    }
  }

  public void testFlush() {
    Exception flushed = Utils.flush(new Flushable() {
      public void flush() throws IOException {
      }
    });
    assertNull(flushed);
    final IOException ioe = new IOException();
    flushed = Utils.flush(new Flushable() {
      public void flush() throws IOException {
        throw ioe;
      }
    });
    assertSame(ioe, flushed);
    final RuntimeException re = new RuntimeException();
    flushed = Utils.flush(new Flushable() {
      public void flush() throws IOException {
        throw re;
      }
    });
    assertSame(re, flushed);
    final Error thrown = new Error();
    try {
      Utils.flush(new Flushable() {
        public void flush() throws IOException {
          throw thrown;
        }
      });
    }
    catch (Error caught) {
      assertSame(thrown, caught);
    }
  }
}
