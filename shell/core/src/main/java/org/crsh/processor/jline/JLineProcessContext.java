package org.crsh.processor.jline;

import org.crsh.shell.ShellProcessContext;
import org.crsh.shell.ShellResponse;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class JLineProcessContext implements ShellProcessContext {

  /** . */
  private static final Character NO_ECHO = (char)0;

  /** . */
  final JLineProcessor processor;

  /** . */
  final CountDownLatch latch = new CountDownLatch(1);

  /** . */
  final AtomicReference<ShellResponse> resp = new AtomicReference<ShellResponse>();

  public JLineProcessContext(JLineProcessor processor) {
    this.processor = processor;
  }

  public int getWidth() {
    return processor.reader.getTerminal().getWidth();
  }

  public String getProperty(String name) {
    return null;
  }

  public String readLine(String msg, boolean echo) {
    try {
      if (echo) {
        return processor.reader.readLine(msg);
      } else {
        return processor.reader.readLine(msg, NO_ECHO);
      }
    }
    catch (IOException e) {
      return null;
    }
  }

  public void end(ShellResponse response) {
    resp.set(response);
    latch.countDown();
  }
}
