import org.crsh.shell.ScriptException;
import org.kohsuke.args4j.Argument;
import org.crsh.shell.Description;

@Description("Add a mixin to a node")
public class addmixin extends org.crsh.shell.ClassCommand {

  @Argument(required=true,index=0,usage="The path of the node to add mixin to")
  def String path;

  @Argument(required=true,index=1,usage="The name of the mixin to add")
  def String mixinName;

  public Object execute() throws ScriptException {
    assertConnected();

    // Get node
    def node = findNodeByPath(path);

    //
    node.addMixin(mixinName);
  }
}