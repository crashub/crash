import org.crsh.shell.ui.UIBuilder
import org.crsh.cmdline.annotations.Usage
import org.crsh.cmdline.annotations.Man
import org.crsh.cmdline.annotations.Command
import org.crsh.cmdline.annotations.Option;
import org.crsh.cmdline.annotations.Argument
import org.crsh.jcr.command.Path

public class ls extends org.crsh.jcr.command.JCRCommand {

  @Usage("list the content of a node")
  @Man("""\
The ls command displays the content of a node. By default it lists the content of the current node, however it also
accepts a path argument that can be absolute or relative.

[/]% ls
/
+-properties
| +-jcr:primaryType: nt:unstructured
| +-jcr:mixinTypes: [exo:owneable,exo:privilegeable]
| +-exo:owner: '__system'
| +-exo:permissions: [any read,*:/platform/administrators read,*:/platform/administrators add_node,*:/platform/administrators set_property,*:/platform/administrators remove]
+-children
| +-/workspace
| +-/contents
| +-/Users
| +-/gadgets
| +-/folder""")
  @Command
  public Object main(
  @Usage("the path to list") @Man("The path of the node content to list") @Argument Path path,
  @Usage("the tree depth") @Man("The depth of the printed tree") @Option(names=["d","depth"]) Integer depth) throws ScriptException {
    assertConnected();

    //
    def node = path == null ? getCurrentNode() : findNodeByPath(path);
    if (depth == null || depth < 1) {
      depth = 1;
    }

    //
    def builder = new UIBuilder();
    formatNode(builder, node, depth, depth);
    return builder;
  }
}