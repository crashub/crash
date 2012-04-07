package org.crsh.cmdline.matcher.impl;

import org.crsh.cmdline.matcher.CmdCompletionException;
import org.crsh.cmdline.spi.CompletionResult;

import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
abstract class Completion {

  abstract CompletionResult<String> complete() throws CmdCompletionException;

}
