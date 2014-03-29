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
package org.crsh.ssh;

import org.crsh.TestPluginLifeCycle;
import org.crsh.auth.AuthenticationPlugin;
import org.crsh.auth.SimpleAuthenticationPlugin;
import org.crsh.term.CodeType;
import org.crsh.term.IOAction;
import org.crsh.term.IOEvent;
import org.crsh.term.IOHandler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class SSHTestCase extends Assert {

  /** . */
  private IOHandler handler;

  /** . */
  private SSHClient client;

  /** . */
  private TestPluginLifeCycle lifeCycle;

  /** We change the port for every test. */
  private static final AtomicInteger PORTS = new AtomicInteger(2000);

  @Before
  public void setUp() throws Exception {

    //
    int port = PORTS.getAndIncrement();

    //
    IOHandler handler = new IOHandler();
    SimpleAuthenticationPlugin auth = new SimpleAuthenticationPlugin();

    //
    TestPluginLifeCycle lifeCycle = new TestPluginLifeCycle(new SSHPlugin(), handler, auth);
    lifeCycle.setProperty(SSHPlugin.SSH_PORT, port);
    lifeCycle.setProperty(SSHPlugin.SSH_SERVER_IDLE_TIMEOUT, 10 * 60 * 1000);
    lifeCycle.setProperty(SSHPlugin.SSH_SERVER_AUTH_TIMEOUT, 10 * 60 * 1000);
    lifeCycle.setProperty(AuthenticationPlugin.AUTH, Arrays.asList(auth.getName()));
    lifeCycle.setProperty(SimpleAuthenticationPlugin.SIMPLE_USERNAME, "root");
    lifeCycle.setProperty(SimpleAuthenticationPlugin.SIMPLE_PASSWORD, "");
    lifeCycle.start();
    SSHClient client = new SSHClient(port).connect();

    //
    this.handler = handler;
    this.client = client;
    this.lifeCycle = lifeCycle;
  }

  @Test
  public void testServerReadAfterClientClose() throws Exception {
    client.write("a").flush();
    handler.add(IOAction.read());
    handler.assertEvent(new IOEvent.IO('a'));

    //
    client.close();
    handler.add(IOAction.read()).add(IOAction.end());
    handler.assertEvent(new IOEvent.IO(CodeType.CLOSE));

    //
    lifeCycle.stop();
  }

  @Test
  public void testClientCloseDuringServerRead() throws Exception {
    client.write("a").flush();
    handler.add(IOAction.read());
    handler.assertEvent(new IOEvent.IO('a'));

    //
    handler.add(IOAction.read()).add(IOAction.end());
    client.close();
    handler.assertEvent(new IOEvent.IO(CodeType.CLOSE));

    //
    lifeCycle.stop();
  }

  @Test
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
    lifeCycle.stop();
  }

  @Test
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
    lifeCycle.stop();
  }

  @Test
  public void testServerWriteChars() throws Exception {
    handler.add(IOAction.write("HOLA"));
    handler.add(IOAction.flush());
    assertEquals('H', client.read());
    assertEquals('O', client.read());
    assertEquals('L', client.read());
    assertEquals('A', client.read());
    lifeCycle.stop();
    assertEquals(-1, client.read());
  }

  @Test
  public void testServerWriteCRLF() throws Exception {
    handler.add(IOAction.crlf());
    handler.add(IOAction.flush());
    assertEquals('\r', client.read());
    assertEquals('\n', client.read());
    handler.add(IOAction.write("\r\n"));
    handler.add(IOAction.flush());
    assertEquals('\r', client.read());
    assertEquals('\n', client.read());
    lifeCycle.stop();
    assertEquals(-1, client.read());
  }

  @Test
  public void testServerWriteDel() throws Exception {
    handler.add(IOAction.del());
    handler.add(IOAction.flush());
    assertEquals('\033', client.read());
    assertEquals('[', client.read());
    assertEquals('D', client.read());
    assertEquals(' ', client.read());
    assertEquals('\033', client.read());
    assertEquals('[', client.read());
    assertEquals('D', client.read());
    lifeCycle.stop();
    assertEquals(-1, client.read());
  }

  @Test
  public void testServerMoveLeft() throws Exception {
    handler.add(IOAction.left());
    handler.add(IOAction.flush());
    assertEquals('\033', client.read());
    assertEquals('[', client.read());
    assertEquals('1', client.read());
    assertEquals('D', client.read());
    lifeCycle.stop();
    assertEquals(-1, client.read());
  }
}
