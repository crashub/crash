import org.crsh.command.Description;

public class invalid extends org.crsh.command.ClassCommand {

  public invalid Object execute() throws ScriptException {
     throw new Exception();
  }
}