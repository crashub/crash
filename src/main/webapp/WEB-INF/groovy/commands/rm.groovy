import org.crsh.shell.ScriptException;
import org.crsh.console.ConsoleBuilder;
import org.kohsuke.args4j.Argument;

public class rm extends org.crsh.shell.ClassCommand {

  @Argument(required=false,index=0,usage="The path of the node to remove")
  def String path;

  public Object execute() throws ScriptException {
    assertConnected();
    def currentNode = getCurrentNode();
    if (currentNode.hasNode(path))
    {
      def node = currentNode.getNode(path);
      node.remove();
    }
    else if (currentNode.hasProperty(path))
    {
      def property = currentNode.getProperty(path);
      property.remove();
    }
    else
      throw new ScriptException("""rm: $path: No such node or property""");
    return null;
  }
}