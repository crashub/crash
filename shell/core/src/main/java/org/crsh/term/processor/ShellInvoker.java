/*
 * Copyright (C) 2011 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 *
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

package org.crsh.term.processor;

import org.crsh.shell.ShellProcessContext;
import org.crsh.shell.ShellResponse;
import org.crsh.term.TermEvent;
import org.crsh.util.LatchedFuture;

import java.io.IOException;

/**
* @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
*/
class ShellInvoker implements ShellProcessContext {

  /** . */
  final LatchedFuture<State> result = new LatchedFuture<State>();

  /** . */
  private final Processor processor;

  public ShellInvoker(Processor processor) {
    this.processor = processor;
  }

  public int getWidth() {
    return processor.term.getWidth();
  }

  public String getProperty(String name) {
    return processor.term.getProperty(name);
  }

  public String readLine(String msg, boolean echo) {
    try {
      processor.term.setEcho(echo);
      processor.term.write(msg);
      TermEvent action = processor.term.read();
      CharSequence line = null;
      if (action instanceof TermEvent.ReadLine) {
        line = ((TermEvent.ReadLine)action).getLine();
        processor.log.debug("Read from console");
      }
      else {
        processor.log.debug("Ignoring action " + action + " returning null");
      }
      processor.term.write("\r\n");
      return line.toString();
    }
    catch (Exception e) {
      processor.log.error("Reading from console failed", e);
      return null;
    }
    finally {
      processor.term.setEcho(true);
    }
  }

  public void end(ShellResponse response) {
    try {

      //
      if (response instanceof ShellResponse.Close) {
        System.out.println("received close response");
        result.set(State.WANT_CLOSE);
      } else {
        if (response instanceof ShellResponse.Cancelled) {
          result.set(State.OPEN);
        } else {
          String ret = response.getText();
          processor.log.debug("Command completed with result " + ret);
          try {
            processor.term.write(ret);
          }
          catch (IOException e) {
            processor.log.error("Write to term failure", e);
          }
          processor.process = null;
        }

        //
        processor.writePrompt();

        //
        result.set(State.OPEN);
      }
    }
    finally {
      processor.process = null;
    }
  }
}
