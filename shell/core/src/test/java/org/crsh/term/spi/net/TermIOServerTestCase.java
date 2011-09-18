package org.crsh.term.spi.net;

import junit.framework.TestCase;
import org.crsh.term.CodeType;
import org.crsh.term.spi.TestTermIO;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
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
    client.close();
    assertFalse(server.execute());
  }
}
