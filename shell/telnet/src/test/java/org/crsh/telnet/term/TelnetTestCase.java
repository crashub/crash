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

package org.crsh.telnet.term;

import junit.framework.TestCase;
import org.apache.commons.net.telnet.TelnetClient;
import org.crsh.TestPluginContext;
import org.crsh.plugin.SimplePluginDiscovery;
import org.crsh.telnet.TelnetPlugin;
import org.crsh.term.CodeType;
import org.crsh.term.IOAction;
import org.crsh.term.IOEvent;
import org.crsh.term.IOHandler;

import java.io.*;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TelnetTestCase extends TestCase {

  /** . */
  private TestPluginContext ctx;

  /** . */
  private TelnetClient client;

  /** . */
  private OutputStream out;

  /** . */
  private IOHandler handler;

  /** . */
  private boolean running;

  @Override
  protected void setUp() throws Exception {

    IOHandler handler = new IOHandler();

    //
    SimplePluginDiscovery discovery = new SimplePluginDiscovery();
    discovery.add(new TelnetPlugin());
    discovery.add(handler);

    //
    ctx = new TestPluginContext(discovery);

    //
    ctx.start();

    //
    TelnetClient client = new TelnetClient();
    client.connect("localhost", 5000);

    //
    this.out = client.getOutputStream();
    this.handler = handler;
    this.client = client;
    this.running = true;
  }

  @Override
  protected void tearDown() throws Exception {
    stop();
  }

  private void stop() {
    if (running) {
      ctx.stop();
      running = false;
    }
  }

  public void testClientDisconnect() throws Exception {
    handler.add(IOAction.read());

    //
    Thread.sleep(10);
    client.disconnect();
    Thread.sleep(10);

    //
    handler.assertEvent(new IOEvent.IO(CodeType.CLOSE));
  }

  public void testServerShutdown() throws Exception {
    out.write(" A".getBytes());
    out.flush();
    handler.add(IOAction.read());
    handler.assertEvent(new IOEvent.IO('A'));

    //
    handler.add(IOAction.read());
    Thread.sleep(10);
    stop();

    //
    handler.assertEvent(new IOEvent.IO(CodeType.CLOSE));
    handler.add(IOAction.end());
  }

  public void testHandlerDisconnect() throws Exception {
    out.write(" A".getBytes());
    out.flush();
    handler.add(IOAction.read());
    handler.assertEvent(new IOEvent.IO('A'));

    //
    handler.add(IOAction.close());
    handler.add(IOAction.end());
    Thread.sleep(10);
    try {
      out.write(" A".getBytes());
      out.flush();
      fail();
    } catch (IOException e) {
    }
  }

  public void testChar() throws Exception {
    out.write(" A".getBytes());
    out.flush();
    handler.add(IOAction.read());
    handler.assertEvent(new IOEvent.IO('A'));
    handler.add(IOAction.end());
  }

  public void testTab() throws Exception {
    out.write(" \t".getBytes());
    out.flush();
    handler.add(IOAction.read());
    handler.assertEvent(new IOEvent.IO(CodeType.TAB));
    handler.add(IOAction.end());
  }

  public void testDelete() throws Exception {
    out.write(" \b".getBytes());
    out.flush();
    handler.add(IOAction.read());
    handler.assertEvent(new IOEvent.IO(CodeType.DELETE));
    handler.add(IOAction.end());
  }
}
