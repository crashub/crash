import javax.jcr.Node

import org.crsh.cli.Command
import org.crsh.cli.Usage
import org.crsh.cli.Man
import org.crsh.jcr.command.Path
import org.crsh.cli.Argument
import org.crsh.command.PipeCommand;

public class mv extends org.crsh.jcr.command.JCRCommand {

  @Command
  @Usage("move a node")
  @Man("""\
The mv command can move a node to a target location in the JCR tree. It can be used also to rename a node. The mv
command is a <Node,Node> command consuming a stream of node to move them and producing nodes that were moved.

[/registry]% mv Registry Registry2""")


  public PipeCommand<Node, Node> main(
    @Argument @Usage("the source path") @Man("The path of the source node to move, absolute or relative") Path source,
    @Argument @Usage("the target path") @Man("The destination path absolute or relative") Path target) {
    assertConnected()

    // Resolve JCR session
    def session = session;
    return new PipeCommand<Node, Node>() {
      @Override
      void open() {
        if (!isPiped()) {
          def sourceNode = findNodeByPath(source);
          def targetPath = absolutePath(target);
          sourceNode.session.workspace.move(sourceNode.path, targetPath.value);
          def targetNode = findNodeByPath(targetPath);
          context.provide(targetNode);
        }
      }
      @Override
      void provide(Node node) {
        def targetParent = findNodeByPath(source);
        def targetPath = targetParent.path + "/" + node.name;
        session.workspace.move(node.path, targetPath);
        context.provide(node);
      }
    }
//
//    //
//    if (context.piped) {
//      if (target != null)
//        throw new ScriptException("Only one argument is permitted when involved in a pipe");
//    } else {
//    }
  }
}