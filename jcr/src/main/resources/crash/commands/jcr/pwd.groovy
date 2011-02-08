import org.crsh.command.InvocationContext
import org.crsh.cmdline.annotations.Command
import org.crsh.cmdline.annotations.Usage
import org.crsh.cmdline.annotations.Man;

public class pwd extends org.crsh.command.CRaSHCommand {

  @Usage("print the current node path")
  @Command
  @Man("""The pwd command prints the current node path, the current node is produced by this command.

[/gadgets]% pwd
/gadgets""")
  public void main(InvocationContext<Node, Void> context) throws ScriptException {
    context.produce(getCurrentNode());
    context.writer <<= currentPath.string;
  }
}