/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.crsh.connector;

import org.codehaus.groovy.runtime.InvokerInvocationException;
import org.crsh.Info;
import org.crsh.display.SimpleDisplayContext;
import org.crsh.display.structure.Element;
import org.crsh.shell.ScriptException;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellBuilder;
import org.crsh.shell.ShellResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ShellConnector {

  /** . */
  private Shell shell;

  /** . */
  private boolean closed;

  public ShellConnector(ShellBuilder builder) {
    this.shell = builder.build();
    this.closed = false;
  }

  public boolean isClosed() {
    return closed;
  }

  public String welcome() throws IOException {
    if (closed) {
      throw new IllegalStateException();
    }
    return "CRaSH " + Info.getVersion() + " (http://crsh.googlecode.com)\r\n" +
    "Welcome to " + InetAddress.getLocalHost().getHostName() + "!\r\n" +
    "It is " + new Date() + " now.\r\n" +
    shell.getPrompt();
  }

  public ShellResponse execute(String request) {
    if (closed) {
      throw new IllegalStateException();
    }
    if ("bye".equals(request)) {
      shell.close();
      closed = true;
      return new ShellResponse.Ok();
    }
    else {
      // Evaluate
      return shell.evaluate(request);
    }
  }

  public String evaluate(String request) {
    String ret = null;
    try {
      // Evaluate
      ShellResponse response = execute(request);

      //
      String result = null;
      if (response instanceof ShellResponse.Error) {
        ShellResponse.Error error = (ShellResponse.Error)response;
        Throwable t = error.getThrowable();
        if (t instanceof Error) {
          throw ((Error)t);
        } else if (t instanceof ScriptException) {
          result = "Error: " + t.getMessage();
        } else if (t instanceof RuntimeException) {
          result = "Unexpected exception: " + t.getMessage();
          t.printStackTrace(System.err);
        } else if (t instanceof Exception) {
          result = "Unexpected exception: " + t.getMessage();
          t.printStackTrace(System.err);
        } else {
          result = "Unexpected throwable: " + t.getMessage();
          t.printStackTrace(System.err);
        }
      } else if (response instanceof ShellResponse.Ok) {

        if (response instanceof ShellResponse.Display) {
          ShellResponse.Display display = (ShellResponse.Display)response;
          SimpleDisplayContext context = new SimpleDisplayContext("\r\n");
          for (Element element : display) {
            element.print(context);
          }
          result = context.getText();
        } else {
          result = "";
        }
      } else if (response instanceof ShellResponse.NoCommand) {
        result = "Please type something";
      } else if (response instanceof ShellResponse.UnkownCommand) {
        ShellResponse.UnkownCommand unknown = (ShellResponse.UnkownCommand)response;
        result = "Unknown command " + unknown.getName();
      }

      // Format response if any
      if (result != null) {
        ret = "" + String.valueOf(result) + "\r\n";
      }
    }
    catch (Throwable t) {
      if (t instanceof InvokerInvocationException) {
        t = t.getCause();
      }
      StringWriter writer = new StringWriter();
      PrintWriter printer = new PrintWriter(writer);
      printer.print("ERROR: ");
      t.printStackTrace(printer);
      printer.println();
      printer.close();
      ret = writer.toString();
    }

    //
    if (ret == null) {
      ret = shell.getPrompt();
    } else {
      ret += shell.getPrompt();
    }

    //
    if (closed) {
      ret += "Have a good day!\r\n";
    }

    //
    return ret;
  }

  public void close() {
    if (!closed) {
      shell.close();
    }
  }
}
