package org.crsh.processor.term;

import org.crsh.plugin.CRaSHPlugin;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellFactory;
import org.crsh.shell.impl.async.AsyncShell;
import org.crsh.term.BaseTerm;
import org.crsh.term.spi.TermIO;
import org.crsh.term.spi.TermIOHandler;

import java.io.Closeable;
import java.security.Principal;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ProcessorIOHandler extends CRaSHPlugin<TermIOHandler> implements TermIOHandler {

  /** . */
  private ShellFactory factory;

  @Override
  public TermIOHandler getImplementation() {
    return this;
  }

  @Override
  public void init() {
    this.factory = getContext().getPlugin(ShellFactory.class);
  }

  @Override
  public void destroy() {
  }

  public void handle(final TermIO io, Principal user) {
    Shell shell = factory.create(user);
    AsyncShell asyncShell = new AsyncShell(getContext().getExecutor(), shell);
    BaseTerm term = new BaseTerm(io);
    Processor processor = new Processor(term, asyncShell);
    processor.addListener(io);
    processor.addListener(asyncShell);
    if (shell instanceof Closeable) {
      processor.addListener((Closeable)shell);
    }
    processor.run();
  }
}