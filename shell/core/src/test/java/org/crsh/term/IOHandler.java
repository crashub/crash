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
package org.crsh.term;

import junit.framework.AssertionFailedError;
import org.crsh.plugin.CRaSHPlugin;
import org.crsh.term.spi.TermIO;
import org.crsh.term.spi.TermIOHandler;

import java.io.IOException;
import java.security.Principal;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class IOHandler extends CRaSHPlugin<TermIOHandler> implements TermIOHandler {

  /** . */
  private final BlockingQueue<IOEvent> eventQueue = new LinkedBlockingQueue<IOEvent>();

  /** . */
  private final BlockingQueue<IOAction> actionQueue = new LinkedBlockingQueue<IOAction>();

  @Override
  public TermIOHandler getImplementation() {
    return this;
  }

  public void handle(TermIO io, Principal user) {
    while (true) {
      IOAction action = null;
      while (action == null) {
        try {
          action = actionQueue.take();
        } catch (InterruptedException e) {
          // We ignore it on purpose
          // as it can come from the client clause
          // but we want to obtain a next event to continue the
          // unit test
        }
      }
      try {
        if (action instanceof IOAction.Read) {
          int code = io.read();
          CodeType codeType = io.decode(code);
          eventQueue.add(new IOEvent.IO(code, codeType));
        } else if (action instanceof IOAction.Write) {
          IOAction.Write write = (IOAction.Write)action;
          io.write(write.s);
        } else if (action instanceof IOAction.Close) {
          io.close();
        } else if (action instanceof IOAction.Flush) {
          io.flush();
        } else if (action instanceof IOAction.CRLF) {
          io.writeCRLF();
        } else if (action instanceof IOAction.Del) {
          io.writeDel();
        } else if (action instanceof IOAction.Left) {
          io.moveLeft();
        } else if (action instanceof IOAction.End) {
          break;
        } else {
          throw new UnsupportedOperationException("Unexpected action " + action);
        }
      } catch (IOException e) {
        e.printStackTrace();
        eventQueue.add(new IOEvent.Error(e));
      }
    }
  }

  public IOHandler add(IOAction action) {
    actionQueue.add(action);
    return this;
  }

  public int getActionCount() {
    return actionQueue.size();
  }

  public void assertEvent(IOEvent expectedEvent) {
    try {
      IOEvent event = eventQueue.poll(2, TimeUnit.SECONDS);
      expectedEvent.assertEquals(event);
    } catch (InterruptedException e) {
      AssertionFailedError afe = new AssertionFailedError();
      afe.initCause(e);
      throw afe;
    }
  }

  public IOEvent take() {
    try {
      return eventQueue.take();
    } catch (InterruptedException e) {
      AssertionFailedError afe = new AssertionFailedError();
      afe.initCause(e);
      throw afe;
    }
  }

  public IOEvent poll() {
    return eventQueue.poll();
  }

  public IOEvent peek() {
    return eventQueue.peek();
  }
}
