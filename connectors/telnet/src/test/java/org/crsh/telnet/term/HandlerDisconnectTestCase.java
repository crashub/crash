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

import org.crsh.term.IOAction;
import org.crsh.term.IOEvent;
import org.junit.Test;

import java.io.IOException;

public class HandlerDisconnectTestCase extends AbstractTelnetTestCase {

  @Test
  public void testHandlerDisconnect() throws Exception {
    out.write(" A".getBytes());
    out.flush();
    handler.add(IOAction.read());
    handler.assertEvent(new IOEvent.IO('A'));

    //
    handler.add(IOAction.close());
    handler.add(IOAction.end());

    // We either get -1 (EOF) or an IOException
    try {
      assertEquals(-1, in.read());
    } catch (IOException e) {
      // OK
    }
  }
}
