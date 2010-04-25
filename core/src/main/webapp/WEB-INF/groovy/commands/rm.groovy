import org.crsh.shell.ScriptException;
import org.kohsuke.args4j.Argument;
import org.crsh.shell.Description;

@Description("Removes a node or a property")
public class rm extends org.crsh.shell.ClassCommand {

  @Argument(required=true,index=0,usage="The path of the node to remove")
  def String path;

  public Object execute() throws ScriptException {
    assertConnected();

    // Get node
    def item = findItemByPath(path);

    //
    item.remove();
  }
}