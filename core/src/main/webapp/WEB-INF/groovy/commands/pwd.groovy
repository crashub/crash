import org.crsh.command.Description;

@Description("Print the current path")
public class pwd extends org.crsh.command.ClassCommand {

  public Object execute() throws ScriptException {
    assertConnected();
    return currentPath;
  }
}