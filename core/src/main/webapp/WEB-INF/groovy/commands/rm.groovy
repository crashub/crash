import org.crsh.command.ScriptException;
import org.kohsuke.args4j.Argument;
import org.crsh.command.Description;
import org.crsh.command.CommandContext;

@Description("Remove one or several node or a property")
public class rm extends org.crsh.command.BaseCommand<Node, Void> {

  @Argument(index=0,usage="The paths of the node to remove")
  def List<String> paths;

  public void execute(CommandContext<Node, Void> context) throws ScriptException {
    assertConnected();

    //
    def nodes = [];
    paths.each { path ->
      def node = getNodeByPath(path);
      if (node == null)
        throw new ScriptException("Node path does not exist");
      nodes.add(node);
    };

    //
    context.writer <<= 'Removed nodes ';

    //
    context.consume().each { node ->
      context.writer <<= " $node.path";
      node.remove();
    };

    // Get node
    nodes.each { node ->
      context.writer <<= " $node.path";
      node.remove();
    };
  }
}