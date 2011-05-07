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
import org.apache.commons.net.telnet.TelnetClient;
import org.crsh.TestPluginContext;
import org.crsh.plugin.CRaSHPlugin;
import org.crsh.plugin.SimplePluginDiscovery;
import org.crsh.telnet.TelnetPlugin;
import org.crsh.term.CodeType;
import org.crsh.term.spi.TermIO;
import org.crsh.term.spi.TermIOHandler;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ReadTestCase extends TestCase {

  /** . */
  private TestPluginContext ctx;

  /** . */
  private CountDownLatch latch;

  /** . */
  private List<Object> read;

  /** . */
  private Throwable failed;

  /** . */
  private OutputStream out;

  private class Handler extends CRaSHPlugin<TermIOHandler> implements TermIOHandler {
    @Override
    public TermIOHandler getImplementation() {
      return this;
    }
    public void handle(TermIO io) {
      try {
        int code = io.read();
        CodeType decode = io.decode(code);
        read.add(decode);
        if (decode == CodeType.CHAR) {
          read.add(code);
        }
      } catch (Throwable t) {
        failed = t;
      } finally {
        io.close();
        latch.countDown();
      }
    }
  }

  protected final void assertDone() throws InterruptedException {
    latch.await();
    if (failed != null) {
      AssertionFailedError afe = new AssertionFailedError();
      afe.initCause(failed);
      throw afe;
    }
  }

  @Override
  protected void setUp() throws Exception {
    SimplePluginDiscovery discovery = new SimplePluginDiscovery();
    discovery.add(new TelnetPlugin());
    discovery.add(new Handler());

    //
    ctx = new TestPluginContext(discovery);
    latch = new CountDownLatch(1);
    read = new ArrayList<Object>();
    failed = null;

    //
    ctx.start();

    //
    TelnetClient client = new TelnetClient();
    client.connect("localhost", 5000);

    //
    out = client.getOutputStream();
  }

  @Override
  protected void tearDown() throws Exception {
    ctx.stop();
  }

  public void testChar() throws Exception {
    out.write(" A".getBytes());
    out.flush();
    assertDone();
    assertEquals(2, read.size());
    assertEquals(CodeType.CHAR, read.get(0));
    assertEquals(65, read.get(1));
  }

  public void testBreak() throws Exception {
    out.write(" \t".getBytes());
    out.flush();
    assertDone();
    assertEquals(1, read.size());
    assertEquals(CodeType.TAB, read.get(0));
  }

  public void testDelete() throws Exception {
    out.write(" \b".getBytes());
    out.flush();
    assertDone();
    assertEquals(1, read.size());
    assertEquals(CodeType.DELETE, read.get(0));
  }
}
