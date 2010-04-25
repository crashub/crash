import org.crsh.shell.Description;

public class invalid extends org.crsh.shell.ClassCommand {

  public invalid Object execute() throws ScriptException {
     throw new Exception();
  }
}