import org.crsh.command.DescriptionMode;

class man extends CRaSHCommand {
  @Usage("format and display the on-line manual pages")
  @Command
  Object main(@Usage("the command") @Argument String command) throws ScriptException {
    def cmd = shell.getCommand(command);
    if (cmd != null) {
      return  cmd.describe(unmatched, DescriptionMode.MAN);
    } else {
      return "Command $command not found";
    }
  }
}