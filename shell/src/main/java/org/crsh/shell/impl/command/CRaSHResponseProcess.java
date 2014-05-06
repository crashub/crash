package org.crsh.shell.impl.command;

import org.crsh.shell.ShellProcessContext;
import org.crsh.shell.ShellResponse;
import org.crsh.text.Text;

import java.io.IOException;

/**
* @author Julien Viet
*/
class CRaSHResponseProcess extends CRaSHProcess {

  /** . */
  private final StringBuilder msg;

  /** . */
  private final ShellResponse response;

  public CRaSHResponseProcess(CRaSHSession session, String request, StringBuilder msg, ShellResponse response) {
    super(session, request);

    //
    this.msg = msg;
    this.response = response;
  }

  @Override
  ShellResponse doInvoke(ShellProcessContext context) throws InterruptedException {
    if (msg.length() > 0) {
      try {
        context.write(Text.create(msg));
      }
      catch (IOException ignore) {
      }
    }
    return response;
  }
}
