import org.crsh.cli.Command
import org.crsh.cli.Usage
import org.crsh.command.PipeCommand;
import javax.jcr.Node;

public class consume {
  @Command
  @Usage("collects and log a set of nodes")
  public PipeCommand<Node, Object> main() {
    return new PipeCommand<Node, Object>() {
      @Override
      void provide(Node element) {
        out.println(element.path);
      }
    }
  }
}