import org.crsh.command.ScriptException;
import org.kohsuke.args4j.Argument;
import org.crsh.command.Description;
import org.crsh.command.CommandContext;

@Description("Removes one or several node or a property")
public class rm extends org.crsh.command.BaseCommand<Node, Void> {

  @Argument(index=0,usage="The paths of the node to remove")
  def List<String> paths;

  public void execute(CommandContext<Node, Void> context) throws ScriptException {
    assertConnected();

    //
    context.writer <<= 'Removed nodes ';

    //
    context.consume().each {
      context.writer <<= " $it.path";
      it.remove();
    };

    // Get node
    paths.each {
      def node = getNodeByPath(it);
      if (node != null) {

      }
      context.writer <<= " $node.path";
      node.remove();
    };
  }
}