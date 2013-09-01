package crash.commands.test

import org.crsh.cli.Argument
import org.crsh.cli.Command
import org.crsh.cli.Usage

public class attribute {

  @Command
  public Object main(@Usage("attribute name") @Argument String argument) {
    return String.valueOf(crash.context.attributes[argument]);
  }
}