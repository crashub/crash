import org.crsh.command.DescriptionMode;

class man extends CRaSHCommand {
  @Usage("format and display the on-line manual pages")
  @Command
  Object main(
    @Usage("command") @Argument String line) throws ScriptException {
    def cmd = shell.getCommand(line);
    if (cmd != null) {
      return  cmd.describe(line, DescriptionMode.MAN);
    } else {
      return "Command $line not found";
    }
  }
}