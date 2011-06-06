package org.crsh.cmdline.matcher.impl;

import org.crsh.cmdline.matcher.CmdCompletionException;

import java.util.Collections;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
class EmptyCompletion extends Completion {

  @Override
  Map<String, String> complete() throws CmdCompletionException {
    return Collections.emptyMap();
  }
}
