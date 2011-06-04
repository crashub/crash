package org.crsh.cmdline.matcher.tokenizer;

import org.crsh.cmdline.matcher.impl.Termination;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public enum Escape {

  NONE(Termination.DETERMINED),

  SINGLE(Termination.SINGLE_QUOTE),

  DOUBLE(Termination.DOUBLE_QUOTE),

  BACKSLASH(Termination.DETERMINED);

  final Termination termination;

  Escape(Termination termination) {
    this.termination = termination;
  }
}
