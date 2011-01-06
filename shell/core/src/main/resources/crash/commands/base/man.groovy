import org.crsh.command.DescriptionMode;

class sleep extends CRaSHCommand {
  @Description(display = "format and display the on-line manual pages")
  @Command
  Object main(
    @Description(display = "command") @Argument String line) throws ScriptException {
    System.out.println(">$line<");
    def cmd = shell.getCommand(line);
    if (cmd != null) {
      return  cmd.describe(line, DescriptionMode.MAN);
    } else {
      return "Command $line not found";
    }
  }
}