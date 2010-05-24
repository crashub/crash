import org.kohsuke.args4j.Argument;
import org.crsh.command.ScriptException;
import org.crsh.command.Description;

@Description("Checkout a node")
public class checkout extends org.crsh.command.ClassCommand {

  @Argument(required=false,index=0,usage="The path of the node to checkout")
  def String path;

  public Object execute() throws ScriptException {
    assertConnected();
    def node = findNodeByPath(path);
    node.checkout();
  }
}
