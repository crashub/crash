import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.Workspace;

import org.crsh.command.ScriptException;
import org.kohsuke.args4j.Argument;
import org.crsh.command.Description;
import org.crsh.command.CommandContext;

@Description("""Move a node to another location. The command can consume the nodes, those nodes will be moved\
instead of the source argument. Any node moved is produced by this command.""")
public class mv extends org.crsh.command.BaseCommand<Node, Node> {

  @Argument(required=false,index=0,usage="The path of the source node to move, absolute or relative")
  def String source;

  @Argument(required=false,index=1,usage="The destination path absolute or relative")
  def String target;

  public void execute(CommandContext<Node, Void> context) throws ScriptException {
    assertConnected()

    //
    if (context.piped) {
      if (target != null)
        throw new ScriptException("Only one argument is permitted when involved in a pipe");
      def targetParent = findNodeByPath(source);
      context.consume().each { node ->
        def targetPath = targetParent.path + "/" + node.name;
        session.workspace.move(node.path, targetPath);
        context.produce(node);
      };
    } else {
      def sourceNode = findNodeByPath(source);
      def targetPath = absolutePath(target);
      sourceNode.session.workspace.move(sourceNode.path, targetPath);
      def targetNode = findNodeByPath(targetPath);
      context.produce(targetNode);
    }
  }
}