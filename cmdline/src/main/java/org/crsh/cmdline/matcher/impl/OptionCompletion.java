package org.crsh.cmdline.matcher.impl;

import org.crsh.cmdline.CommandCompletion;
import org.crsh.cmdline.CommandDescriptor;
import org.crsh.cmdline.Termination;
import org.crsh.cmdline.matcher.CmdCompletionException;
import org.crsh.cmdline.matcher.tokenizer.Token;
import org.crsh.cmdline.spi.ValueCompletion;

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
  protected CommandCompletion complete() throws CmdCompletionException {
    ValueCompletion completions = new ValueCompletion(prefix.getValue());
    Set<String> optionNames = prefix instanceof Token.Literal.Option.Short ? descriptor.getShortOptionNames() : descriptor.getLongOptionNames();
    for (String optionName : optionNames) {
      if (optionName.startsWith(prefix.getValue())) {
        completions.put(optionName.substring(prefix.getValue().length()), true);
      }
    }
    return new CommandCompletion(Termination.DETERMINED, completions);
  }
}
