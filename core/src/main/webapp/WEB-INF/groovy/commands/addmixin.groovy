import org.kohsuke.args4j.Argument;
import org.crsh.command.ScriptException;
import org.crsh.command.Description;
import org.crsh.command.CommandContext;

@Description("Add a mixin to one or several nodes")
public class addmixin extends org.crsh.command.BaseCommand<Node, Void> {

  @Argument(required=true,index=0,usage="The name of the mixin to add")
  def String mixinName;

  @Argument(index=1,usage="The paths of the node to add mixin to")
  def List<String> paths;

  public void execute(CommandContext<Node, Void> context) throws ScriptException {
    assertConnected();

    //
    def ret = 'Added mixin $mixinName to nodes';

    //
    context.consume().each {
      ret <<= " $it.path";
      it.addMixin(mixinName);
    };

    //
    paths.each {
      def node = getNodeByPath(it);
      ret <<= " $node.path";
      node.addMixin(mixinName);
    };

    //
    context.getWriter().print(ret);
  }
}