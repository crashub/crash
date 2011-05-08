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

import junit.framework.AssertionFailedError;
import org.crsh.plugin.CRaSHPlugin;
import org.crsh.term.CodeType;
import org.crsh.term.spi.TermIO;
import org.crsh.term.spi.TermIOHandler;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class FooTermIOHandler extends CRaSHPlugin<TermIOHandler> implements TermIOHandler {

  /** . */
  private final BlockingQueue<TermEvent> eventQueue = new LinkedBlockingQueue<TermEvent>();

  /** . */
  private final BlockingQueue<TermAction> actionQueue = new LinkedBlockingQueue<TermAction>();

  @Override
  public TermIOHandler getImplementation() {
    return this;
  }

  public void handle(TermIO io) {
    while (true) {
      TermAction action = null;
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
        if (action instanceof TermAction.Read) {
          int code = io.read();
          CodeType codeType = io.decode(code);
          eventQueue.add(new TermEvent.IO(code, codeType));
        } else if (action instanceof TermAction.Write) {
          TermAction.Write write = (TermAction.Write)action;
          io.write(write.s);
        } else if (action instanceof TermAction.Close) {
          io.close();
        } else if (action instanceof TermAction.Flush) {
          io.flush();
        } else if (action instanceof TermAction.CRLF) {
          io.writeCRLF();
        } else if (action instanceof TermAction.Del) {
          io.writeDel();
        } else if (action instanceof TermAction.End) {
          break;
        } else {
          throw new UnsupportedOperationException("Unexpected action " + action);
        }
      } catch (IOException e) {
        e.printStackTrace();
        eventQueue.add(new TermEvent.Error(e));
      }
    }
  }

  public FooTermIOHandler add(TermAction action) {
    actionQueue.add(action);
    return this;
  }

  public void assertEvent(TermEvent expectedEvent) {
    try {
      TermEvent event = eventQueue.poll(10, TimeUnit.SECONDS);
      expectedEvent.assertEquals(event);
    } catch (InterruptedException e) {
      AssertionFailedError afe = new AssertionFailedError();
      afe.initCause(e);
      throw afe;
    }
  }

  public TermEvent take() {
    try {
      return eventQueue.take();
    } catch (InterruptedException e) {
      AssertionFailedError afe = new AssertionFailedError();
      afe.initCause(e);
      throw afe;
    }
  }

  public TermEvent poll() {
    return eventQueue.poll();
  }

  public TermEvent peek() {
    return eventQueue.peek();
  }
}
