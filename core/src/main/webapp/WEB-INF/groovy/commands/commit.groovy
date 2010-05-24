import org.kohsuke.args4j.Argument;
import org.crsh.command.ScriptException;
import org.crsh.command.Description;

@Description("Saves the changes done to the current session or node")
public class commit extends org.crsh.command.ClassCommand {

  @Argument(required=false,index=0,usage="The path of the node to commit")
  def String path;

  public Object execute() throws ScriptException {
    assertConnected();
    def node = findNodeByPath(path);
    node.save();
  }
}
