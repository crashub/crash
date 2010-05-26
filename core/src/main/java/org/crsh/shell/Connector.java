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
package org.crsh.shell;

import org.crsh.Info;
import org.crsh.command.ScriptException;
import org.crsh.display.SimpleDisplayContext;
import org.crsh.display.structure.Element;
import org.crsh.util.ImmediateFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Connector {

  /** . */
  private final Logger log = LoggerFactory.getLogger(Connector.class);

  /** . */
  private ConnectorStatus status;

  /** . */
  private Future<ShellResponse> futureResponse;

  /** . */
  private final Object lock;

  /** . */
  private final Shell shell;

  /** . */
  private final ExecutorService executor;

  public Connector(Shell shell) {
    this(null, shell);
  }

  public Connector(ExecutorService executor, Shell shell) {
    this.executor = executor;
    this.shell = shell;
    this.status = ConnectorStatus.INITIAL;
    this.lock = new Object();
  }

  public boolean isClosed() {
    return status == ConnectorStatus.CLOSED;
  }

  public String open() {
    if (status != ConnectorStatus.INITIAL) {
      throw new IllegalStateException();
    }
    String hostName;
    try {
      hostName = InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      hostName = "localhost";
    }
    String ret = "CRaSH " + Info.getVersion() + " (http://crsh.googlecode.com)\r\n" +
        "Welcome to " + hostName + "!\r\n" +
        "It is " + new Date() + " now.\r\n" +
        shell.getPrompt();
    status = ConnectorStatus.AVAILABLE;
    return ret;
  }

  public String getPrompt() {
    return shell.getPrompt();
  }

  public ConnectorStatus getStatus() {
    return status;
  }

  public void submitEvaluation(String request) {
    submitEvaluation(request, null);
  }

  public void submitEvaluation(final String request, final ConnectorResponseContext handler) {

    // The response context
    final ShellResponseContext responseContext = new ShellResponseContext() {
      public String readLine(String msg) {
        return handler.readLine(msg);
      }
    };

    //
    synchronized (lock) {
      if (status != ConnectorStatus.AVAILABLE) {
        throw new IllegalStateException("State was " + status);
      }

      //
      status = ConnectorStatus.EVALUATING;

      //
      Callable<ShellResponse> callable;
      if ("bye".equals(request)) {
        callable = new Callable<ShellResponse>() {
          public ShellResponse call() throws Exception {
            return new ShellResponse.Close();
          }
        };
      } else {
        callable = new Callable<ShellResponse>() {
          public ShellResponse call() throws Exception {
            return shell.evaluate(request, responseContext);
          }
        };
      }

      //
      Bilto bilto = new Bilto(this, handler, callable);

      // Execute it with or without an executor
      if (executor != null) {
        // log.debug("Submitting to executor");
        futureResponse = executor.submit(bilto);
      } else {
        try {
          ShellResponse response = bilto.call();
          futureResponse =  new ImmediateFuture<ShellResponse>(response);
        } catch (Exception e) {
          AssertionError afe = new AssertionError("Should not happen cause we are calling Evaluable");
          afe.initCause(e);
          throw afe;
        }
      }
    }
  }

  public boolean cancelEvalutation() {
    synchronized (lock) {
      if (status == ConnectorStatus.EVALUATING) {
        status = ConnectorStatus.AVAILABLE;
        futureResponse.cancel(true);
        futureResponse = null;
        return true;
      } else {
        return false;
      }
    }
  }

  /**
   * <p>Updates the state of the connector when it is in <code>ConnectorStatus#EVALUATING</code> status. When the connector
   * is succesfully updated, the status becomes <code>ConnectorStatus#AVAILABLE</code> and the text corresponding to
   * the shell response is returned.</p>
   *
   * @param responseContext the response context
   * @param response the response
   */
  void update(ConnectorResponseContext responseContext, ShellResponse response) {
    synchronized (lock) {

      //
      String ret = null;

      //
      switch (status) {
        // We were waiting for that response
        case EVALUATING:
          try {
            ret = decode(response);
            futureResponse = null;
            break;
          } finally {
            status = ConnectorStatus.AVAILABLE;
          }
        case AVAILABLE:
          // A cancelled command likely
          break;
        case CLOSED:
          // Long running command that comes after the connector was closed
          break;
        default:
        case INITIAL:
          throw new AssertionError("That should not be possible");
      }

      //
      if (responseContext != null) {
        log.debug("Making handler response callback with " + ret);
        responseContext.completed(ret);
      }

      //
      if (response instanceof ShellResponse.Close) {
        if (responseContext != null) {
          log.debug("Signaling close to response context");
          responseContext.close();
        }
      }
    }
  }

  public String popResponse() {

    //
    ShellResponse response = null;

    //
    if (futureResponse != null) {
      try {
        // That will trigger the completion handler and the state update
        // of this object so there is no need to do anything else than that
        response = futureResponse.get();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    //
    if (response != null) {
      return decode(response);
    } else {
      return null;
    }
  }

  public String evaluate(String request) {
    submitEvaluation(request, null);
    return popResponse();
  }

  public void close() {
    synchronized (lock) {
      switch (status) {
        case INITIAL:
        case AVAILABLE:
          break;
        case EVALUATING:
          throw new UnsupportedOperationException("todo :-)");
        case CLOSED:
          break;
      }
      status = ConnectorStatus.CLOSED;
    }
  }

  private String decode(ShellResponse response) {
    String ret = null;
    try {
      String result = null;
      if (response instanceof ShellResponse.Close) {
        ret = "Have a good day!\r\n";
      } if (response instanceof ShellResponse.Error) {
        ShellResponse.Error error = (ShellResponse.Error) response;
        Throwable t = error.getThrowable();
        if (t instanceof Error) {
          throw ((Error) t);
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
          ShellResponse.Display display = (ShellResponse.Display) response;
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
        ShellResponse.UnkownCommand unknown = (ShellResponse.UnkownCommand) response;
        result = "Unknown command " + unknown.getName();
      }

      // Format response if any
      if (result != null) {
        ret = "" + String.valueOf(result) + "\r\n";
      }
    } catch (Throwable t) {
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
      ret = getPrompt();
    } else {
      ret += getPrompt();
    }

    //
    return ret;
  }
}
