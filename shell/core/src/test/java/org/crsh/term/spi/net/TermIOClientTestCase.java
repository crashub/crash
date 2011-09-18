package org.crsh.term.spi.net;

import junit.framework.TestCase;
import org.crsh.term.CodeType;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
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
  }

}
