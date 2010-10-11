import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import org.crsh.command.ScriptException;
import org.crsh.command.Description;
import org.crsh.command.CommandContext;

@Description("Collect a set of nodes")
public class addnode extends org.crsh.command.BaseCommand<Void, Node> {
  @Argument(required=false,index=0)
  def List<String> paths;
  public void execute(CommandContext<Void, Node> context) throws ScriptException {
    assertConnected();
    paths.each {
      def node = getNodeByPath(it);
      context.produce(node);
    }
  }
}