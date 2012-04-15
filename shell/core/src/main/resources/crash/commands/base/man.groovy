import org.crsh.command.DescriptionFormat
import org.crsh.command.CRaSHCommand
import org.crsh.cmdline.annotations.Usage
import org.crsh.cmdline.annotations.Command
import org.crsh.cmdline.annotations.Argument;

class man extends CRaSHCommand {
  @Usage("format and display the on-line manual pages")
  @Command
  Object main(@Usage("the command") @Argument String command) throws ScriptException {
    def cmd = crash.getCommand(command);
    if (cmd != null) {
      return  cmd.describe(unmatched, DescriptionFormat.MAN);
    } else {
      return "Command $command not found";
    }
  }
}