package org.crsh.ssh.term.inline;

import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellProcessContext;
import org.crsh.shell.ShellResponse;
import org.crsh.ssh.term.SSHContext;
import org.crsh.text.Chunk;
import org.crsh.text.Text;

import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.CountDownLatch;

/** ShellProcessContext for SSH inline commands */
public class SSHInlineShellProcessContext implements ShellProcessContext {

  /** . */
  private static final String MSG = "Cannot determine tty width : you should force pseudo-tty allocation (-t option)";

  /** . */
  private boolean msgDone;

  /** . */
  private ShellResponse response;

  /** . */
  private final CountDownLatch latch;

  /** . */
  private final SSHContext context;

  /** . */
  private final ShellProcess process;

  /** . */
  private final PrintStream out;

  /** . */
  private final PrintStream err;

  SSHInlineShellProcessContext(SSHContext context, ShellProcess process, PrintStream out, PrintStream err) {
    this.out = out;
    this.context = context;
    this.process = process;
    this.latch = new CountDownLatch(1);
    this.response = null;
    this.err = err;
    this.msgDone = false;
  }

  public SSHInlineShellProcessContext execute() {
    process.execute(this);
    return this;
  }

  public boolean takeAlternateBuffer() {
    return false;
  }

  public boolean releaseAlternateBuffer() {
    return false;
  }

  public int getWidth() {
    int width = context.getWidth();
    if (width == -1) {
      if (!msgDone) {
        msgDone = true;
        out.print(MSG);
        out.flush();
      }
    }
    return width;
  }

  public int getHeight() {
    int height = context.getHeight();
    if (height == -1) {
      if (!msgDone) {
        msgDone = true;
        out.print(MSG);
        out.flush();
        }
    }
    return height;
  }

  public String getProperty(String name) {
    return context.getProperty(name);
  }

  public String readLine(String msg, boolean echo) {
    return null;
  }

  public Class<Chunk> getConsumedType() {
    return Chunk.class;
  }

  public void provide(Chunk element) throws IOException {
    if (element instanceof Text) {
      CharSequence seq = ((Text)element).getText();
      int length = seq.length();
      if (length > 0) {
        for (int i = 0;i < length;i++) {
          // This is not perfect but it will be OK for now
          // ideally we should reuse the IO / ConsoleTerm stuff
          // but for now we don't have the time to do it properly
          char c = seq.charAt(i);
          if (c == '\r') {
            //
          } else if (c == '\n') {
            out.print("\r\n");
          } else {
            out.print(c);
          }
        }
      }
    }
  }

  public void flush() throws IOException {
    out.flush();
  }

  public void end(ShellResponse response) {
    this.response = response;
    this.latch.countDown();
  }

  ShellResponse getResponse() {
    try {
      latch.await();
      return response;
    }
    catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

}
