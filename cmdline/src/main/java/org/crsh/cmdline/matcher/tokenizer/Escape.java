package org.crsh.cmdline.matcher.tokenizer;

import org.crsh.cmdline.Termination;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
enum Escape {

  NONE(Termination.DETERMINED),

  SINGLE(Termination.SINGLE_QUOTE),

  DOUBLE(Termination.DOUBLE_QUOTE),

  BACKSLASH(Termination.DETERMINED);

  final Termination termination;

  Escape(Termination termination) {
    this.termination = termination;
  }
}
