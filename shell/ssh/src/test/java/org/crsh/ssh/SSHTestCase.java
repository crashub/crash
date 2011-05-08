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
package org.crsh.ssh;

import junit.framework.TestCase;
import org.crsh.TestPluginContext;
import org.crsh.plugin.PropertyDescriptor;
import org.crsh.plugin.SimplePluginDiscovery;
import org.crsh.term.IOAction;
import org.crsh.term.IOEvent;
import org.crsh.term.IOHandler;
import org.crsh.term.CodeType;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class SSHTestCase extends TestCase {

  /** . */
  private IOHandler handler;

  /** . */
  private SSHClient client;

  /** . */
  private TestPluginContext ctx;

  /** We change the port for every test. */
  private static final AtomicInteger PORTS = new AtomicInteger(2000);

  @Override
  protected void setUp() throws Exception {

    //
    int port = PORTS.getAndIncrement();

    //
    IOHandler handler = new IOHandler();
    SimplePluginDiscovery discovery = new SimplePluginDiscovery();
    discovery.add(new SSHPlugin());
    discovery.add(handler);
    TestPluginContext ctx = new TestPluginContext(discovery);
    ctx.setProperty(PropertyDescriptor.SSH_PORT, port);
    ctx.start();
    SSHClient client = new SSHClient(port).connect();

    //
    this.handler = handler;
    this.client = client;
    this.ctx = ctx;
  }

  public void testServerReadAfterClientClose() throws Exception {
    client.write("a").flush();
    handler.add(IOAction.read());
    handler.assertEvent(new IOEvent.IO('a'));

    //
    client.close();
    handler.add(IOAction.read()).add(IOAction.end());
    handler.assertEvent(new IOEvent.IO(CodeType.CLOSE));

    //
    ctx.stop();
  }

  public void testClientCloseDuringServerRead() throws Exception {
    client.write("a").flush();
    handler.add(IOAction.read());
    handler.assertEvent(new IOEvent.IO('a'));

    //
    handler.add(IOAction.read()).add(IOAction.end());
    client.close();
    handler.assertEvent(new IOEvent.IO(CodeType.CLOSE));

    //
    ctx.stop();
  }

  public void testClientWrite() throws Exception {
    client.write("HELLO").flush();
    handler.add(IOAction.read());
    handler.add(IOAction.read());
    handler.add(IOAction.read());
    handler.add(IOAction.read());
    handler.add(IOAction.read());
    handler.add(IOAction.end());
    handler.assertEvent(new IOEvent.IO('H'));
    handler.assertEvent(new IOEvent.IO('E'));
    handler.assertEvent(new IOEvent.IO('L'));
    handler.assertEvent(new IOEvent.IO('L'));
    handler.assertEvent(new IOEvent.IO('O'));

    //
    client.close();
    ctx.stop();
  }

  public void testServerClose() throws Exception {
    client.write("a").flush();
    handler.add(IOAction.read());
    handler.assertEvent(new IOEvent.IO('a'));

    //
    handler.add(IOAction.close()).add(IOAction.read()).add(IOAction.end());
    handler.assertEvent(new IOEvent.IO(CodeType.CLOSE));

    //
    try {
      client.write("foo");
      fail();
    } catch (IOException ignore) {
    }

    //
    ctx.stop();
  }

  public void testServerWriteChars() throws Exception {
    handler.add(IOAction.write("HOLA"));
    handler.add(IOAction.flush());
    Thread.sleep(10);
    String s = client.read();
    assertEquals("HOLA", s);
    ctx.stop();
  }

  public void testServerWriteCRLF() throws Exception {
    handler.add(IOAction.crlf());
    handler.add(IOAction.flush());
    Thread.sleep(10);
    assertEquals("\r\n", client.read());
    handler.add(IOAction.write("\r\n"));
    handler.add(IOAction.flush());
    Thread.sleep(10);
    assertEquals("\r\n", client.read());
    ctx.stop();
  }

  public void testServerWriteDel() throws Exception {
    handler.add(IOAction.del());
    handler.add(IOAction.flush());
    Thread.sleep(10);
    assertEquals("\b \b", client.read());
    ctx.stop();
  }

  public void testServerMoveLeft() throws Exception {
    handler.add(IOAction.del());
    handler.add(IOAction.flush());
    Thread.sleep(10);
    assertEquals("\b \b", client.read());
    ctx.stop();
  }
}
