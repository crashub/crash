import org.kohsuke.args4j.Argument;
import org.crsh.command.ScriptException;
import org.crsh.command.Description;

@Description("Checkin a node")
public class checkin extends org.crsh.command.ClassCommand {

  @Argument(required=false,index=0,usage="The path of the node to checkin")
  def String path;

  public Object execute() throws ScriptException {
    assertConnected();
    def node = findNodeByPath(path);
    node.checkin();
  }
}
