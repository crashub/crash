import org.crsh.shell.ScriptException;
import org.kohsuke.args4j.Argument;

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