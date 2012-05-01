package org.crsh.processor.term;

import org.crsh.plugin.CRaSHPlugin;
import org.crsh.shell.impl.async.AsyncShell;
import org.crsh.shell.impl.command.CRaSH;
import org.crsh.shell.impl.command.CRaSHSession;
import org.crsh.term.BaseTerm;
import org.crsh.term.spi.TermIO;
import org.crsh.term.spi.TermIOHandler;

import java.security.Principal;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ProcessorIOHandler extends CRaSHPlugin<TermIOHandler> implements TermIOHandler {

  /** . */
  private CRaSH crash;

  @Override
  public TermIOHandler getImplementation() {
    return this;
  }

  @Override
  public void init() {
    this.crash = new CRaSH(getContext());
  }

  @Override
  public void destroy() {
  }

  public void handle(final TermIO io, Principal user) {
    CRaSHSession shell = crash.createSession(user);
    AsyncShell asyncShell = new AsyncShell(getContext().getExecutor(), shell);
    BaseTerm term = new BaseTerm(io);
    Processor processor = new Processor(term, asyncShell);
    processor.addListener(io);
    processor.addListener(asyncShell);
    processor.addListener(shell);
    processor.run();
  }
}