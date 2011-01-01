import org.crsh.command.Description;

@Description("Fails")
public class fail extends org.crsh.command.ClassCommand {

  public Object execute() throws ScriptException {
     throw new Exception();
  }
}