import org.crsh.command.Description;
import org.crsh.command.InvocationContext;

@Description("Print the current path. The current node is produced by this command")
public class pwd extends org.crsh.command.BaseCommand<Void, Node> {

  public void execute(InvocationContext<Node, Void> context) throws ScriptException {
    context.produce(getCurrentNode());

    //
    context.writer <<= currentPath;
  }
}