import org.crsh.shell.Description;

@Description("Print the current path")
public class pwd extends org.crsh.shell.ClassCommand {

  public Object execute() throws ScriptException {
    assertConnected();
    return currentPath;
  }
}