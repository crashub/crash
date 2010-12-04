import org.crsh.command.ScriptException;
import org.kohsuke.args4j.Argument;
import org.crsh.command.Description;
import org.crsh.command.CommandContext;

@Description("Remove one or several node or a property. This command can also consume a stream of node\
to remove.")
public class rm extends org.crsh.command.BaseCommand<Node, Void> {

  @Argument(index=0,usage="The paths of the node to remove")
  def List<String> paths;

  public void execute(CommandContext<Node, Void> context) throws ScriptException {
    assertConnected();

    //
    context.writer <<= 'Removed nodes ';

    //
    if (context.piped) {
      if (paths != null && paths.empty)
        throw new ScriptException("No path arguments are permitted in a pipe");

      // Node stream
      context.consume().each { node ->
        context.writer <<= " $node.path";
        node.remove();
      };
    } else {
      // First collect nodes
      def nodes = [];
      paths.each { path ->
        def node = getNodeByPath(path);
        if (node == null)
          throw new ScriptException("Node path does not exist");
        nodes.add(node);
      };
      // Then remove if we have been able to find them all
      nodes.each { node ->
        context.writer <<= " $node.path";
        node.remove();
      };
    }
  }
}