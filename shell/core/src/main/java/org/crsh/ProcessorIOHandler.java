package org.crsh;

import org.crsh.plugin.CRaSHPlugin;
import org.crsh.shell.concurrent.AsyncShell;
import org.crsh.shell.impl.CRaSH;
import org.crsh.term.BaseTerm;
import org.crsh.term.spi.TermIO;
import org.crsh.term.spi.TermIOHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ProcessorIOHandler extends CRaSHPlugin<TermIOHandler> implements TermIOHandler {

  /** . */
  private ExecutorService executor;

  @Override
  public TermIOHandler getImplementation() {
    return this;
  }

  @Override
  public void init() {
    this.executor = Executors.newFixedThreadPool(3);
  }

  @Override
  public void destroy() {
    if (executor != null) {
      executor.shutdown();
    }
  }

  public void handle(final TermIO io) {
    final CRaSH shell = new CRaSH(getContext());
    final AsyncShell asyncShell = new AsyncShell(executor, shell);
    BaseTerm term = new BaseTerm(io);
    Processor processor = new Processor(term, asyncShell);

    //
    processor.addListener(new ProcessorListener() {
      public void closed() {
        io.close();
      }
    });

    //
    processor.addListener(new ProcessorListener() {
      public void closed() {
        asyncShell.close();
      }
    });

    //
    processor.addListener(new ProcessorListener() {
      public void closed() {
        shell.close();
      }
    });

    //
    processor.run();
  }
}
