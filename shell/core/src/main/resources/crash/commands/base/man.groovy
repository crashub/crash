import org.crsh.command.DescriptionFormat
import org.crsh.command.CRaSHCommand
import org.crsh.cli.Usage
import org.crsh.cli.Command
import org.crsh.cli.Argument;

class man extends CRaSHCommand {
  @Usage("format and display the on-line manual pages")
  @Command
  Object main(@Usage("the command") @Argument String command) {
    def cmd = crash.getCommand(command);
    if (cmd != null) {
      return  cmd.describe(unmatched, DescriptionFormat.MAN);
    } else {
      return "Command $command not found";
    }
  }
}