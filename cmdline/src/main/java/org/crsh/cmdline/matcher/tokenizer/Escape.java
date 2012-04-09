package org.crsh.cmdline.matcher.tokenizer;

import org.crsh.cmdline.Delimiter;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
enum Escape {

  NONE(Delimiter.EMPTY),

  SINGLE(Delimiter.SINGLE_QUOTE),

  DOUBLE(Delimiter.DOUBLE_QUOTE),

  BACKSLASH(Delimiter.EMPTY);

  final Delimiter delimiter;

  Escape(Delimiter delimiter) {
    this.delimiter = delimiter;
  }
}
