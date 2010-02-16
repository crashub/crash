import org.crsh.shell.Description;

@Description("Fails")
public class fail extends org.crsh.shell.ClassCommand {

  public Object execute() throws ScriptException {
     throw new Exception();
  }
}