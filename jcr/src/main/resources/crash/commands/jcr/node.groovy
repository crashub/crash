import org.crsh.jcr.command.PathArg;

public class node extends org.crsh.jcr.command.JCRCommand {

  @Usage("creates one or several nodes")
  @Man("""\
The addnode command creates one or several nodes. The command takes at least one node as argument, but it can
 take more. Each path can be either absolute or relative, relative path creates nodes relative to the current node.
 By default the node type is the default repository node type, but the option -t can be used to specify another one.

[/registry]% addnode foo
Node /foo created

[/registry]% addnode -t nt:file bar juu
Node /bar /juu created

The addnode command is a <Void,Node> command that produces all the nodes that were created.""")
  @Command
  public void add(
    InvocationContext<Void, Node> context,
    @Usage("the paths to be created")
    @Man("The paths of the new node to be created, the paths can either be absolute or relative.")
    @PathArg List<String> paths,
    @Usage("the node type name")
    @Man("The name of the primary node type to create.")
    @Option(names=["t","type"]) String primaryNodeTypeName)
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