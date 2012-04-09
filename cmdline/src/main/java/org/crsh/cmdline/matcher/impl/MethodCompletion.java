package org.crsh.cmdline.matcher.impl;

import org.crsh.cmdline.ClassDescriptor;
import org.crsh.cmdline.CommandCompletion;
import org.crsh.cmdline.Delimiter;
import org.crsh.cmdline.MethodDescriptor;
import org.crsh.cmdline.matcher.CmdCompletionException;
import org.crsh.cmdline.spi.ValueCompletion;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
class MethodCompletion<T> extends Completion {

  /** . */
  private final ClassDescriptor<T> descriptor;

  /** . */
  private final String mainName;

  /** . */
  private final  String prefix;

  /** . */
  private final Delimiter delimiter;

  MethodCompletion(ClassDescriptor<T> descriptor, String mainName, String prefix, Delimiter delimiter) {
    this.descriptor = descriptor;
    this.mainName = mainName;
    this.prefix = prefix;
    this.delimiter = delimiter;
  }

  @Override
  protected CommandCompletion complete() throws CmdCompletionException {
    ValueCompletion completions = new ValueCompletion(prefix);
    for (MethodDescriptor<?> m : descriptor.getMethods()) {
      String name = m.getName();
      if (name.startsWith(prefix)) {
        if (!name.equals(mainName)) {
          completions.put(name.substring(prefix.length()), true);
        }
      }
    }
    return new CommandCompletion(delimiter, completions);
  }
}
