import org.kohsuke.args4j.Argument;
import org.crsh.command.ScriptException;
import org.crsh.command.Description;
import org.crsh.command.CommandContext;

@Description("""Add a mixin to one or several nodes. It consumes a node stream or path arguments.""")
public class addmixin extends org.crsh.command.BaseCommand<Node,Void> {

  @Argument(required=true,metaVar="mixin name",index=0,usage="The name of the mixin to add")
  def String mixinName;

  @Argument(index=1,metaVar="mixin paths",usage="The paths of the node to add mixin to")
  def List<String> paths;

  public void execute(CommandContext<Node, Void> context) throws ScriptException {
    assertConnected();

    //
    context.writer <<= "Mixin $mixinName added to nodes";

    //
    if (context.piped) {
      if (paths != null && !paths.empty) {
        throw new ScriptException("No path arguments are permitted in a pipe");
      }
      context.consume().each { node ->
        context.writer <<= " $node.path";
        node.addMixin(mixinName);
      };
    } else {
      paths.each { path ->
        def node = getNodeByPath(path);
        context.writer <<= " $node.path";
        node.addMixin(mixinName);
      };
    }
  }
}