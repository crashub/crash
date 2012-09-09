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

package org.crsh.term.spi.net;

import junit.framework.TestCase;
import org.crsh.term.CodeType;
import org.crsh.term.spi.TestTermIO;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class TermIOServerTestCase extends TestCase {

  public void testFoo() throws IOException {

    TestTermIO tt = new TestTermIO();
    TermIOServer server = new TermIOServer(tt, 1500);
    server.bind();

    //
    Socket client = new Socket();
    client.connect(new InetSocketAddress(1500));
    OutputStream out = client.getOutputStream();
    InputStream in = client.getInputStream();

    //
    server.accept();

    //
    out.write(0);
    out.flush();
    tt.append('a');
    assertTrue(server.execute());
    assertEquals(CodeType.CHAR.ordinal(), in.read());
    assertEquals(0, in.read());
    assertEquals(97, in.read());

    //
    out.write(0);
    out.flush();
    tt.appendDel();
    assertTrue(server.execute());
    assertEquals(CodeType.BACKSPACE.ordinal(), in.read());

    //
    out.write(1);
    out.write(0);
    out.write(97);
    out.flush();
    assertTrue(server.execute());
    tt.assertChars("a");

    //
    out.write(2);
    out.write(1);
    out.write(0);
    out.write('f');
    out.write(0);
    out.write('o');
    out.write(0);
    out.write('o');
    out.flush();
    assertTrue(server.execute());
    tt.assertChars("foo");

    //
    out.write(3);
    out.flush();
    assertTrue(server.execute());
    tt.assertDel();

    //
    out.write(4);
    out.flush();
    assertTrue(server.execute());
    tt.assertCRLF();

    //
    out.write(5);
    out.write(0);
    out.write(97);
    out.flush();
    assertTrue(server.execute());
    tt.assertMoveRight();

    //
    out.write(6);
    out.flush();
    assertTrue(server.execute());
    tt.assertMoveLeft();

    // Cannot test flush for now
//    out.write(7);
//    out.flush();
//    server.execute();

    //
    out.write(8);
    out.write(0);
    out.write('a');
    out.flush();
    assertTrue(server.execute());
    assertEquals(0, in.read());

    //
    tt.setProperty("a", "b");
    out.write(8);
    out.write(0);
    out.write('a');
    out.flush();
    assertTrue(server.execute());
    assertEquals(2, in.read());
    assertEquals('b', in.read());

    out.write(8);
    out.write(4);
    out.write('w');
    out.write('i');
    out.write('d');
    out.write('t');
    out.write('h');
    out.flush();
    assertTrue(server.execute());
    assertEquals(3, in.read());
    assertEquals('3', in.read());
    assertEquals('2', in.read());

    //
    client.close();
    assertFalse(server.execute());
  }
}
