class help extends CRaSHCommand
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
    shellContext.listResourceId(org.crsh.plugin.ResourceKind.SCRIPT).eachWithIndex() {
      cmd, index ->
      if (index > 0)
        ret += ",";
      ret += cmd;
    }
    ret += ")";
    return ret;
  }
}