import org.crsh.command.ScriptException;
import org.kohsuke.args4j.Argument;
import org.crsh.command.Description;

@Description("Removes one or several node or a property")
public class rm extends org.crsh.command.ClassCommand {

  @Argument(index=0,usage="The paths of the node to remove")
  def List<String> paths;

  public Object execute() throws ScriptException {
    assertConnected();

    // Get node
    def ret = 'Node';
    paths.each {
      def node = getNodeByPath(it);
      ret <<= " $node.path";
      node.remove();
    };

    //
    return "$ret removed";
  }
}