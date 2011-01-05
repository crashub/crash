import org.crsh.command.DescriptionMode;

class help extends CRaSHCommand
{

  /** . */
  private static final String TAB = "  ";

  /** . */
  private static final List<String> commands = [
  "addnode",
  "addmixin",
  "cd",
  "checkin",
  "checkout",
  "commit",
  "connect",
  "disconnect",
  "exportnode",
  "help",
  "importnode",
  "ls",
  "mv",
  "pwd",
  "rm",
  "rollback",
  "select",
  "set",
  "setperm",
  "xpath"];

  @Description(display = "Provides basic help")
  @Command
  Object main() {
    def ret = "Try one of these commands with the -h or --help switch:\n\n";
    shellContext.listResourceId(org.crsh.plugin.ResourceKind.SCRIPT).each() {
      cmdName ->
    try {
        def cmd = shell.getCommand(cmdName);
        if (cmd != null) {
          def desc = cmd.describe(cmdName, DescriptionMode.DESCRIBE) ?: "";
          ret += "$TAB$cmdName $desc\n";
        }
      } catch (org.crsh.shell.impl.CreateCommandException ignore) {
      }
    }
    ret += "\n";
    return ret;
  }
}