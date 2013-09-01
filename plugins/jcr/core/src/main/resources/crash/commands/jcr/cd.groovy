import org.crsh.cli.Usage
import org.crsh.cli.Command
import org.crsh.cli.Man
import org.crsh.cli.Argument
import org.crsh.jcr.command.Path;

public class cd extends org.crsh.jcr.command.JCRCommand {

  @Usage("changes the current node")
  @Command
  @Man("""\
The cd command changes the current node path. The command used with no argument changes to the root
node. A relative or absolute path argument can be provided to specify a new current node path.

[/]% cd /gadgets
[/gadgets]% cd /gadgets
[/gadgets]% cd
[/]%""")
  public Object main(
    @Argument
    @Usage("the new path")
    @Man("The new path that will change the current node navigation") Path path) {
    assertConnected();
    def node = findNodeByPath(path);
    setCurrentNode(node);
  }
}

