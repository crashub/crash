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
package org.crsh.shell.connector;

import org.crsh.shell.Shell;
import org.crsh.shell.ShellResponse;
import org.crsh.shell.ShellResponseContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

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
  private FutureEvaluation futureEvaluation;

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
    String ret = shell.getWelcome();
    status = ConnectorStatus.AVAILABLE;
    return ret;
  }

  public String getPrompt() {
    try {
      return shell.getPrompt();
    } catch (Exception e) {
      log.error("Could not obtain prompt", e);
      return "% ";
    }
  }

  public ConnectorStatus getStatus() {
    synchronized (lock) {
      return status;
    }
  }

  public Future<ShellResponse> submitEvaluation(String request) {
    return submitEvaluation(request, null);
  }

  public Future<ShellResponse> submitEvaluation(final String request, final ConnectorResponseContext handler) {

    // The response context
    final ShellResponseContext responseContext = new ShellResponseContext() {
      public String readLine(String msg, boolean echo) {
        return handler.readLine(msg, echo);
      }
    };

    //
    synchronized (lock) {
      if (status != ConnectorStatus.AVAILABLE) {
        throw new IllegalStateException("State was " + status);
      }

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
      ResponseEvaluation evaluation = new ResponseEvaluation(this, handler, callable);

      // Execute it with or without an executor
      Future<ShellResponse> clientResponse;
      if (executor != null) {
        // log.debug("Submitting to executor");
        Future<ShellResponse> futureResponse = executor.submit(evaluation);

        // Update state
        futureEvaluation =  new FutureEvaluation(futureResponse, handler);
        status = ConnectorStatus.EVALUATING;

        //
        clientResponse = futureResponse;
      } else {
        try {
          FutureTask<ShellResponse> futureResponse = new FutureTask<ShellResponse>(evaluation);

          // Update state
          futureEvaluation =  new FutureEvaluation(futureResponse, handler);
          status = ConnectorStatus.EVALUATING;

          // Trigger call
          futureResponse.run();
          clientResponse = futureResponse;
        } catch (Exception e) {
          AssertionError afe = new AssertionError("Should not happen");
          afe.initCause(e);
          throw afe;
        }
      }

      //
      return clientResponse;
    }
  }

  public boolean cancelEvalutation() {

    //
    FutureEvaluation evaluation = null;
    synchronized (lock) {
      if (status == ConnectorStatus.EVALUATING) {
        evaluation = this.futureEvaluation;
        status = ConnectorStatus.AVAILABLE;
        futureEvaluation = null;
      }
    }

    //
    if (evaluation != null) {
      //
      evaluation.futureResponse.cancel(true);
      if (evaluation.responseHandler != null) {
        evaluation.responseHandler.done(false);
      }
      return  true;
    } else {
      return false;
    }
  }

  /**
   * <p>Updates the state of the connector when it is in <code>ConnectorStatus#EVALUATING</code> status.
   * When the connector is succesfully updated, the status becomes <code>ConnectorStatus#AVAILABLE</code>.</p>
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
            ret = response.getText() /*+ "\r\n" + getPrompt()*/;
            futureEvaluation = null;
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
        if (ret != null) {
          log.debug("Making handler response callback with " + ret);
          responseContext.completed(ret);
        }

        // Obtain prompt
        String prompt = getPrompt();

        //
        responseContext.setPrompt(prompt);

        //
        log.debug("Signaling done to response context");
        responseContext.done(response instanceof ShellResponse.Close);
      }
    }
  }

  public String popResponse() {

    //
    ShellResponse response = null;

    //
    if (futureEvaluation != null) {
      try {
        // That will trigger the completion handler and the state update
        // of this object so there is no need to do anything else than that
        response = futureEvaluation.futureResponse.get();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    //
    if (response != null) {
      return response.getText();
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
}
