package org.crsh.processor.term;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class SyncProcessorTestCase extends AbstractProcessorTestCase {

  @Override
  protected SyncTerm createTerm() {
    return new SyncTerm();
  }

  @Override
  protected SyncShell createShell() {
    return new SyncShell();
  }

  @Override
  protected Processor createProcessor(SyncTerm term, SyncShell shell) {
    return new Processor(term, shell);
  }

  @Override
  protected int getBarrierSize() {
    return 1;
  }
}
