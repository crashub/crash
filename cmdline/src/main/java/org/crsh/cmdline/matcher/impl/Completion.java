package org.crsh.cmdline.matcher.impl;

import org.crsh.cmdline.CommandCompletion;
import org.crsh.cmdline.matcher.CmdCompletionException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
abstract class Completion {

  abstract CommandCompletion complete() throws CmdCompletionException;

}
