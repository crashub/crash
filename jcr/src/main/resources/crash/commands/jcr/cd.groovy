import org.crsh.jcr.command.PathArg;

import org.crsh.cmdline.ParameterDescriptor;
import javax.jcr.Session;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Item;

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
  public Object main(@PathArg String path) throws ScriptException {
    assertConnected();
    def node = findNodeByPath(path);
    setCurrentNode(node);
  }
}

