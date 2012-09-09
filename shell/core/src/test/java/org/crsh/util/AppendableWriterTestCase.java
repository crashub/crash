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
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.concurrent.atomic.AtomicInteger;

public class AppendableWriterTestCase extends TestCase {
  
  public void testWrite() throws IOException {
    StringBuilder buffer = new StringBuilder();
    AppendableWriter writer = new AppendableWriter(buffer);

    //
    writer.write("foo");
    assertEquals("foo", buffer.toString());
    buffer.setLength(0);

    //
    writer.write('a');
    assertEquals("a", buffer.toString());
    buffer.setLength(0);

    //
    writer.write("bar".toCharArray(), 0, 1);
    assertEquals("b", buffer.toString());
    buffer.setLength(0);
  }
  
  public void testClose() throws IOException {
    StringBuilder buffer = new StringBuilder();
    AppendableWriter writer = new AppendableWriter(buffer);
    
    //
    writer.close();
    try {
      writer.write("foo");
      fail();
    } catch (IOException expected) {
      assertEquals("", buffer.toString());
    }

    //
    writer.close();
    try {
      writer.flush();
      fail();
    } catch (IOException expected) {
      assertEquals("", buffer.toString());
    }

    //
    writer.close();
  }
  
  public void testPropagateClose() throws IOException {
    final AtomicInteger closed = new AtomicInteger();
    StringWriter buffer = new StringWriter() {
      @Override
      public void close() throws IOException {
        closed.incrementAndGet();
        super.close();
      }
    };
    AppendableWriter writer = new AppendableWriter(buffer, (Closeable)buffer);
    assertEquals(0, closed.get());
    writer.close();
    assertEquals(1, closed.get());
    writer.close();
    assertEquals(1, closed.get());
  }
  
  public void testIOException() throws IOException {
    final IOException ioe1 = new IOException();
    Writer buffer = new Writer() {
      @Override
      public void write(char[] cbuf, int off, int len) throws IOException {
        throw ioe1;
      }
      @Override
      public void flush() throws IOException {
      }
      @Override
      public void close() throws IOException {
      }
    };
    AppendableWriter writer = new AppendableWriter(buffer, (Closeable)buffer);
    try {
      writer.write("foo");
      fail();
    } catch (IOException e) {
      assertSame(ioe1, e);
    }
    
    //
    final IOException ioe2 = new IOException();
    final AtomicInteger count = new AtomicInteger();
    buffer = new Writer() {
      @Override
      public void write(char[] cbuf, int off, int len) throws IOException {
      }
      @Override
      public void flush() throws IOException {
      }
      @Override
      public void close() throws IOException {
        count.incrementAndGet();
        if (count.get() == 1) {
          throw ioe2;
        }
      }
    };
    writer = new AppendableWriter(buffer, (Closeable)buffer);
    assertEquals(0, count.get());
    try {
      writer.close();
      fail();
    } catch (IOException e) {
      assertEquals(1, count.get());
      assertSame(ioe2, e);
    }
    writer.close();
    assertEquals(1, count.get());
  }
}
