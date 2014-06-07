package org.crsh.shell.impl.command;

import org.crsh.keyboard.KeyHandler;
import org.crsh.shell.ErrorKind;
import org.crsh.shell.ShellProcessContext;
import org.crsh.shell.ShellResponse;
import org.crsh.shell.impl.command.spi.CommandException;
import org.crsh.shell.impl.command.spi.CommandInvoker;
import org.crsh.util.Utils;

/**
* @author Julien Viet
*/
class CRaSHCommandProcess extends CRaSHProcess {

  /** . */
  private final CRaSHSession session;

  /** . */
  private final CommandInvoker<Void, ?> command;

  public CRaSHCommandProcess(CRaSHSession session, String request, CommandInvoker<Void, ?> command) {
    super(session, request);

    //
    this.session = session;
    this.command = command;
  }

  @Override
  public KeyHandler getKeyHandler() {
    return command.getKeyHandler();
  }

  @Override
  ShellResponse doInvoke(final ShellProcessContext context) throws InterruptedException {
    CRaSHProcessContext invocationContext = new CRaSHProcessContext(session, context);
    try {
      command.invoke(invocationContext);
      return ShellResponse.ok();
    }
    catch (CommandException e) {
      return build(e);
    } catch (Throwable t) {
      return build(t);
    } finally {
      Utils.close(invocationContext);
    }
  }

  private ShellResponse.Error build(Throwable throwable) {
    ErrorKind errorType;
    if (throwable instanceof CommandException) {
      CommandException ce = (CommandException)throwable;
      errorType = ce.getErrorKind();
      Throwable cause = throwable.getCause();
      if (cause != null) {
        throwable = cause;
      }
    } else {
      errorType = ErrorKind.INTERNAL;
    }
    String result;
    String msg = throwable.getMessage();
    if (throwable instanceof CommandException) {
      if (msg == null) {
        result = request + ": failed";
      } else {
        result = request + ": " + msg;
      }
      return ShellResponse.error(errorType, result, throwable);
    } else {
      if (msg == null) {
        msg = throwable.getClass().getSimpleName();
      }
      if (throwable instanceof RuntimeException) {
        result = request + ": exception: " + msg;
      } else if (throwable instanceof Exception) {
        result = request + ": exception: " + msg;
      } else if (throwable instanceof Error) {
        result = request + ": error: " + msg;
      } else {
        result = request + ": unexpected throwable: " + msg;
      }
      return ShellResponse.error(errorType, result, throwable);
    }
  }
}
