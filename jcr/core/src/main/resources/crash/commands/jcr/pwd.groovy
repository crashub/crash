import org.crsh.command.InvocationContext
import org.crsh.cli.Command
import org.crsh.cli.Usage
import org.crsh.cli.Man;

public class pwd {

  @Usage("print the current node path")
  @Command
  @Man("""The pwd command prints the current node path, the current node is produced by this command.

[/gadgets]% pwd
/gadgets""")
  public void main(InvocationContext<Node> context) {
    context.provide(getCurrentNode());
    out << currentPath.value;
  }
}