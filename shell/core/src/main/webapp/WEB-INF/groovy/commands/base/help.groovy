import org.crsh.command.Description;
import org.crsh.cmdline.Command;

@Description("Provides basic help")
class help extends org.crsh.command.CRaSHCommand
{

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

  @Command
  Object main() {
    def ret = "Try one of these commands with the -h or --help switch (";
    shellContext.listResourceId(org.crsh.shell.ResourceKind.SCRIPT).eachWithIndex() {
      cmd, index ->
      if (index > 0)
        ret += ",";
      ret += cmd;
    }
    ret += ")";
    return ret;
  }
}