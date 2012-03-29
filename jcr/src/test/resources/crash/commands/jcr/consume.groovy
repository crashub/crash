import org.crsh.command.ScriptException;
import org.crsh.command.InvocationContext
import org.crsh.cmdline.annotations.Command
import org.crsh.cmdline.annotations.Usage;

public class consume extends org.crsh.command.CRaSHCommand {
  @Command
  @Usage("collects a set of nodes")
  public void main(InvocationContext<Node, Node> context) throws ScriptException {
    context.consume().each {
      context.produce(it);
    }
  }
}