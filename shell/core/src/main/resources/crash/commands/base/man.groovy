import org.crsh.command.DescriptionMode;

class sleep extends CRaSHCommand {
  @Description("format and display the on-line manual pages")
  @Command
  Object main(
    @Description("command") @Argument String line) throws ScriptException {
    def cmd = shell.getCommand(line);
    if (cmd != null) {
      return  cmd.describe(line, DescriptionMode.MAN);
    } else {
      return "Command $line not found";
    }
  }
}