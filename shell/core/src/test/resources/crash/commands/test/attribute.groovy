package crash.commands.test

import org.crsh.cmdline.annotations.Argument
import org.crsh.cmdline.annotations.Command
import org.crsh.cmdline.annotations.Usage

public class attribute extends org.crsh.command.CRaSHCommand {

  @Command
  public Object main(@Usage("attribute name") @Argument String argument) throws ScriptException {
    return String.valueOf(crash.context.attributes[argument]);
  }
}