import org.crsh.command.ScriptException;
import org.crsh.cli.Command
import org.crsh.cli.Usage
import org.crsh.cli.Man
import org.crsh.jcr.command.Path
import org.crsh.cli.Argument
import org.crsh.command.PipeCommand;
import javax.jcr.Node;

public class rm extends org.crsh.jcr.command.JCRCommand {

  @Command
  @Usage("remove one or several node or a property")
  @Man("""\
The rm command removes a node or property specified by its path either absolute or relative. This operation
is executed against the JCR session, meaning that it will not be effective until it is commited to the JCR server.

[/]% rm foo
Node /foo removed

It is possible to specify several nodes.

[/]% rm foo bar
Node /foo /bar removed

rm is a <Node,Void> command removing all the consumed nodes.""")
  public PipeCommand<Node, Void> main(
    @Argument @Usage("the paths to remove") @Man("The paths of the node to remove") List<Path> paths) {
    assertConnected();
    def foo = out;
    return new PipeCommand<Node, Void>() {
      @Override
      void open() {
        foo << 'Removed nodes ';
        if (!isPiped()) {
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
            foo << " $node.path";
            node.remove();
          };
        }
      }

      @Override
      void provide(Node node) {
        foo << " $node.path";
        node.remove();
      }
    }
  }
}