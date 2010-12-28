import org.crsh.jcr.command.PathArg;

import org.crsh.command.ScriptException;
import org.crsh.command.Description;
import org.crsh.command.InvocationContext;

public class addnode extends org.crsh.jcr.command.JCRCommand {

  @Command(description = "Creates one or several nodes. It produces all the nodes that were created by the command.")
  public void main(
    InvocationContext<Void, Node> context,
    // The paths of the new node to be created, the paths can either be absolute or relative.
    @PathArg List<String> paths,
    @Option(names=["t","types"],description="The name of the primary node type to create") String primaryNodeTypeName)
    throws ScriptException {
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

