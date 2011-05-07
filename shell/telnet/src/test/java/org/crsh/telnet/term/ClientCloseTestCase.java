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

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import net.wimpi.telnetd.io.TerminalIO;
import org.crsh.TestPluginContext;
import org.crsh.plugin.CRaSHPlugin;
import org.crsh.plugin.SimplePluginDiscovery;
import org.crsh.telnet.TelnetPlugin;
import org.crsh.term.spi.TermIO;
import org.crsh.term.spi.TermIOHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ClientCloseTestCase extends TestCase {

  /** . */
  private TestPluginContext ctx;

  /** . */
  private final CountDownLatch latch1 = new CountDownLatch(1);

  /** . */
  private final CountDownLatch latch2 = new CountDownLatch(1);

  /** . */
  private Thread thread;

  /** . */
  private List<Integer> read = new ArrayList<Integer>();

  /** . */
  private Throwable failed;

  private class Handler extends CRaSHPlugin<TermIOHandler> implements TermIOHandler {
    @Override
    public TermIOHandler getImplementation() {
      return this;
    }
    public void handle(TermIO io) {
      thread = Thread.currentThread();
      latch1.countDown();
      try {
        int i = io.read();
        read.add(i);
      } catch (Throwable t) {
        failed = t;
      } finally {
        latch2.countDown();
      }
    }
  }

  @Override
  protected void setUp() throws Exception {
    SimplePluginDiscovery discovery = new SimplePluginDiscovery();
    discovery.add(new TelnetPlugin());
    discovery.add(new Handler());

    //
    ctx = new TestPluginContext(discovery);
    ctx.start();
  }

  @Override
  protected void tearDown() throws Exception {
    ctx.stop();
  }

  public void testMain() throws Exception {
    Socket socket = new Socket();
    socket.connect(new InetSocketAddress("localhost", 5000));
    latch1.await();
    Thread.sleep(10); // yes it's not deterministic, but well...
    socket.close();
    latch2.await();
    if (failed != null) {
      AssertionFailedError afe = new AssertionFailedError();
      afe.initCause(failed);
      throw afe;
    }
    assertEquals(1, read.size());
    assertEquals(TerminalIO.HANDLED, (int)read.get(0));
  }
}
