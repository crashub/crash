import org.crsh.shell.Description;

@Description("Disconnect from the current repository")
public class disconnect extends org.crsh.shell.ClassCommand {

  public Object execute() throws ScriptException {
    assertConnected();
    session.logout();
    session = null;
    currentPath = null;
    return "Disconnected from workspace";
  }
}
