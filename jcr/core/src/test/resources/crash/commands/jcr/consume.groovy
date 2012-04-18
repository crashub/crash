import org.crsh.command.InvocationContext
import org.crsh.cmdline.annotations.Command
import org.crsh.cmdline.annotations.Usage;

public class consume extends org.crsh.command.CRaSHCommand {
  @Command
  @Usage("collects a set of nodes")
  public void main(InvocationContext<Node, Node> context) {
    context.consume().each {
      context.produce(it);
    }
  }
}