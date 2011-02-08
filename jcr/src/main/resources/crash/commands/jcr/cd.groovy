import org.crsh.cmdline.annotations.Usage
import org.crsh.cmdline.annotations.Command
import org.crsh.cmdline.annotations.Man
import org.crsh.cmdline.annotations.Argument
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
    @Man("The new path that will change the current node navigation") Path path) throws ScriptException {
    assertConnected();
    def node = findNodeByPath(path);
    setCurrentNode(node);
  }
}

