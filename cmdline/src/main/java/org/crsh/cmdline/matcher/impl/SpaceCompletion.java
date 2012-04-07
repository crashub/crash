package org.crsh.cmdline.matcher.impl;

import org.crsh.cmdline.matcher.CmdCompletionException;
import org.crsh.cmdline.spi.CompletionResult;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
class SpaceCompletion extends Completion {

  @Override
  CompletionResult<String> complete() throws CmdCompletionException {
    return CompletionResult.create("", " ");
  }
}
