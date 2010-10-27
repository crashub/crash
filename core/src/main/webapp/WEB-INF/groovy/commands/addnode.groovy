import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import org.crsh.command.ScriptException;
import org.crsh.command.Description;
import org.crsh.command.CommandContext;

@Description("Creates one or several nodes. It produces all the nodes that were created by the command.")
public class addnode extends org.crsh.command.BaseCommand<Void, Node> {

  @Argument(required=true,index=0,usage="The paths of the new node to be created, the paths can either be absolute or relative.")
  def List<String> paths;

  @Option(name="-t",aliases=["--type"],usage="The name of the primary node type to create")
  def String primaryNodeTypeName;

  public void execute(CommandContext<Void, Node> context) throws ScriptException {
    assertConnected();

    //
    context.writer <<= 'Node';
    paths.each {

      def parent;
      if (it.charAt(0) == '/') {
        parent = session.rootNode;
        it = it.substring(1);
      } else {
        parent = getCurrentNode();
      }

      //
      def node;
      if (primaryNodeTypeName != null) {
        node = parent.addNode(it, primaryNodeTypeName);
      } else {
        node = parent.addNode(it);
      }

      //
      context.produce(node);

      //
      context.writer <<= " $node.path";
    }

    //
    context.writer <<= " created";
  }
}

