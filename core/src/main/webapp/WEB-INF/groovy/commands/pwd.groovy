import org.crsh.command.Description;
import org.crsh.command.CommandContext;

@Description("Print the current path")
public class pwd extends org.crsh.command.BaseCommand<Void, Node> {

  public void execute(CommandContext<Node, Void> context) throws ScriptException {
    context.produce(getCurrentNode());

    //
    context.writer <<= currentPath;
  }
}