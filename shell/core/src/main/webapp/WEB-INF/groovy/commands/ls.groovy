import org.crsh.shell.ui.UIBuilder;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import org.crsh.command.Description;

@Description("List the content of a node")
public class ls extends org.crsh.command.ClassCommand {

  @Argument(required=false,index=0,usage="Path of the node content to list")
  def String path;

  @Option(name="-d",aliases=["--depth"],usage="Print depth")
  def Integer depth;

  public Object execute() throws ScriptException {
    assertConnected();

    //
    def node = path == null ? getCurrentNode() : findNodeByPath(path);

    //
    def builder = new UIBuilder();

    //
    if (depth == null || depth < 1) {
      depth = 1;
    }

    //
    formatNode(builder, node, depth, depth);

    //
    return builder;
  }
}