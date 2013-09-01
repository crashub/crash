import org.crsh.command.InvocationContext
import org.crsh.cli.Command
import org.crsh.cli.Usage
import org.crsh.jcr.command.Path
import org.crsh.cli.Argument;
import javax.jcr.Node;

public class produce {

  @Command
  @Usage("produce a set of nodes")
  public void main(
    InvocationContext<Node> context,
    @Argument @Usage("the paths") List<Path> paths) {
    assertConnected();
    paths.each {
      try {
        def node = getNodeByPath(it)
        context.provide(node);
      }
      catch (Throwable t) {
        t.printStackTrace();
      }
    }
  }
}