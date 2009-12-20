import org.crsh.console.ConsoleBuilder;

public class pwd extends org.crsh.shell.ClassCommand {

  public Object execute() throws ScriptException {
    assertConnected();
    def builder = new ConsoleBuilder();
    builder.message(currentPath);
    return builder;
  }
}