import org.crsh.console.ConsoleBuilder;
{ String commandName ->


  if (commandName == null) {
    def builder = new ConsoleBuilder();

    //
    builder.table {
      row("CRaSH version 1.0")
      row("connect $workspace-name : connect to a workspace")
      row("disconnect : disconnect from the current workspace")
      row("cd $path : change the current path")
      row("ls : list the properties and children of the current node")
      row("pwd : the current node path")
      row("bye : quit the shell")
    }

    //
    return builder;
  } else {
    def command = this[commandName];
    if (command instanceof Closure) {
      def res = "Command " + commandName;
      command.parameterTypes.each() { type ->
        res += " " + type.simpleName;
      }
      return res;
      return "Command " + command;
    } else {
      return "Unknown command " + commandName;
    }
  }
}
