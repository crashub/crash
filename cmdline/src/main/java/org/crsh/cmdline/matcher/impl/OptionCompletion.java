package org.crsh.cmdline.matcher.impl;

import org.crsh.cmdline.CommandDescriptor;
import org.crsh.cmdline.matcher.CmdCompletionException;
import org.crsh.cmdline.matcher.tokenizer.Token;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
class OptionCompletion<T> extends Completion {

  /** . */
  private final CommandDescriptor<T, ?> descriptor;

  /** . */
  private final Token.Literal.Option prefix;

  OptionCompletion(CommandDescriptor<T, ?> descriptor, Token.Literal.Option prefix) {
    this.descriptor = descriptor;
    this.prefix = prefix;
  }

  @Override
  protected Map<String, String> complete() throws CmdCompletionException {
    Map<String, String> completions = new HashMap<String, String>();
    Set<String> optionNames = prefix instanceof Token.Literal.Option.Short ? descriptor.getShortOptionNames() : descriptor.getLongOptionNames();
    for (String optionName : optionNames) {
      if (optionName.startsWith(prefix.getValue())) {
        completions.put(optionName.substring(prefix.getValue().length()), " ");
      }
    }
    return completions;
  }
}
