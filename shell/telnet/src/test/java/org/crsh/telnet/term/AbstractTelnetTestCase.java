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

package org.crsh.telnet.term;

import org.apache.commons.net.telnet.TelnetClient;
import org.crsh.TestPluginLifeCycle;
import org.crsh.plugin.SimplePluginDiscovery;
import org.crsh.telnet.TelnetPlugin;
import org.crsh.term.IOHandler;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;

import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(BMUnitRunner.class)
public abstract class AbstractTelnetTestCase extends Assert {

  /** . */
  protected TestPluginLifeCycle ctx;

  /** . */
  protected TelnetClient client;

  /** . */
  protected OutputStream out;

  /** . */
  protected InputStream in;

  /** . */
  protected IOHandler handler;

  /** . */
  private boolean running;

  /** . */
  private static final AtomicInteger PORTS = new AtomicInteger(5000);

  /** . */
  private static final int CLIENT_CONNECT_RETRY_LIMIT = 5;
  private static final long CLIENT_CONNECT_RETRY_SLEEP = 1000;

  @Before
  public final void setUp() throws Exception {

    int port = PORTS.getAndIncrement();

    //
    IOHandler handler = new IOHandler();

    //
    SimplePluginDiscovery discovery = new SimplePluginDiscovery();
    discovery.add(new TelnetPlugin());
    discovery.add(handler);

    //
    ctx = new TestPluginLifeCycle(new TelnetPlugin(), handler);
    ctx.setProperty(TelnetPlugin.TELNET_PORT, port);

    //
    ctx.start();

    //
    TelnetClient client = new TelnetClient();
    for (int retry_count = 0; retry_count < CLIENT_CONNECT_RETRY_LIMIT; retry_count++) {
      try {
        client.connect("localhost", port);
        break;
      } catch (IOException e) {
        if (retry_count < CLIENT_CONNECT_RETRY_LIMIT) {
          Thread.sleep(CLIENT_CONNECT_RETRY_SLEEP);
        } else {
          throw e;
        }
      }
    }

    //
    this.out = client.getOutputStream();
    this.in = client.getInputStream();
    this.handler = handler;
    this.client = client;
    this.running = true;
  }

  @After
  public final void tearDown() throws Exception {
    stop();
  }

  protected final void stop() {
    if (running) {
      ctx.stop();
      running = false;
    }
  }
}
