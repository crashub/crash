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

import org.crsh.term.CodeType;
import org.crsh.term.IOAction;
import org.crsh.term.IOEvent;
import org.jboss.byteman.contrib.bmunit.BMScript;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

@RunWith(BMUnitRunner.class)
public class ServerShutdownTestCase extends AbstractTelnetTestCase {

  /** . */
  public static transient boolean wantCountDown = false;

  /** . */
  public static final CountDownLatch latch = new CountDownLatch(1);

  @Test
  @BMScript(dir = "src/test/resources", value = "serverShutdown")
  public void testServerShutdown() throws Exception {
    out.write(" A".getBytes());
    out.flush();
    handler.add(IOAction.read());
    handler.assertEvent(new IOEvent.IO('A'));

    //
    wantCountDown = true;
    handler.add(IOAction.read());
    latch.await();
    stop();

    //
    handler.assertEvent(new IOEvent.IO(CodeType.CLOSE));
    handler.add(IOAction.end());
  }
}
