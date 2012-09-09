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

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TermIOClientTestCase extends TestCase {

  public void testFoo() throws Exception {

    ServerSocket socketServer = new ServerSocket();
    socketServer.bind(new InetSocketAddress(0));
    int port = socketServer.getLocalPort();
    System.out.println("Bound on port " + port);

    TermIOClient client = new TermIOClient(port);
    client.connect();

    Socket serverSocket = socketServer.accept();
    OutputStream out = serverSocket.getOutputStream();
    InputStream in = serverSocket.getInputStream();

    //
    out.write(new byte[]{(byte) CodeType.CHAR.ordinal(),0,97});
    out.flush();
    int r = client.read();
    assertEquals(CodeType.CHAR, client.decode(r));
    assertEquals('a', r);
    assertEquals(0, in.read());

    //
    out.write(CodeType.BACKSPACE.ordinal());
    out.flush();
    assertEquals(CodeType.BACKSPACE, client.decode(client.read()));
    assertEquals(0, in.read());

    //
    client.write('b');
    client.flush();
    assertEquals(1, in.read());
    assertEquals(0, in.read());
    assertEquals(98, in.read());
    assertEquals(7, in.read());

    //
    client.write("ab");
    client.flush();
    assertEquals(2, in.read());
    assertEquals(0, in.read());
    assertEquals(0, in.read());
    assertEquals(97, in.read());
    assertEquals(0, in.read());
    assertEquals(98, in.read());
    assertEquals(7, in.read());

    //
    StringBuilder sb = new StringBuilder();
    for (int i = 0;i < 258;i++) {
      sb.append('a');
    }
    client.write(sb.toString());
    client.flush();
    assertEquals(2, in.read());
    assertEquals(255, in.read());
    for (int i = 0;i < 257;i++) {
      assertEquals(0, in.read());
      assertEquals(97, in.read());
    }
    assertEquals(1, in.read());
    assertEquals(0, in.read());
    assertEquals(97, in.read());
    assertEquals(7, in.read());

    //
    client.writeDel();
    client.flush();
    assertEquals(3, in.read());
    assertEquals(7, in.read());

    //
    client.writeCRLF();
    client.flush();
    assertEquals(4, in.read());
    assertEquals(7, in.read());

    //
    assertTrue(client.moveRight('d'));
    client.flush();
    assertEquals(5, in.read());
    assertEquals(0, in.read());
    assertEquals(100, in.read());
    assertEquals(7, in.read());

    //
    assertTrue(client.moveLeft());
    client.flush();
    assertEquals(6, in.read());
    assertEquals(7, in.read());

    //
    assertEquals(null, client.getProperty(""));

    //
    out.write(0);
    assertEquals(null, client.getProperty("a"));
    assertEquals(8, in.read());
    assertEquals(0, in.read());
    assertEquals('a', in.read());

    //
    out.write(1);
    assertEquals("", client.getProperty("a"));
    assertEquals(8, in.read());
    assertEquals(0, in.read());
    assertEquals('a', in.read());

    //
    out.write(2);
    out.write('b');
    assertEquals("b", client.getProperty("a"));
    assertEquals(8, in.read());
    assertEquals(0, in.read());
    assertEquals('a', in.read());

    //
    out.write(3);
    out.write('3');
    out.write('2');
    assertEquals(32, client.getWidth());
    assertEquals(8, in.read());
    assertEquals(4, in.read());
    assertEquals('w', in.read());
    assertEquals('i', in.read());
    assertEquals('d', in.read());
    assertEquals('t', in.read());
    assertEquals('h', in.read());
  }

}
