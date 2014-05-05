import org.crsh.cli.Command
import org.crsh.cli.Usage
import org.crsh.command.Pipe;
import javax.jcr.Node;

public class consume {
  @Command
  @Usage("collects and log a set of nodes")
  public Pipe<Object, String> main() {
    return new Pipe<Object, String>() {
      @Override
      void provide(Object element) {
        if (element instanceof Node) {
          Node node = (Node)element;
          out.println(node.path);
        }
      }
    }
  }
}