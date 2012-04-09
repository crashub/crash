package org.crsh.cmdline.matcher.impl;

import org.crsh.cmdline.CommandCompletion;
import org.crsh.cmdline.Delimiter;
import org.crsh.cmdline.matcher.CmdCompletionException;
import org.crsh.cmdline.spi.ValueCompletion;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
class EmptyCompletion extends Completion {

  @Override
  CommandCompletion complete() throws CmdCompletionException {
    return new CommandCompletion(Delimiter.EMPTY, ValueCompletion.create());
  }
}
