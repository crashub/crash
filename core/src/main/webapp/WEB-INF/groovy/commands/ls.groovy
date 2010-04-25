import org.crsh.display.DisplayBuilder;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import org.crsh.shell.Description;

@Description("List the content of a node")
public class ls extends org.crsh.shell.ClassCommand {

  @Argument(required=false,index=0,usage="Path of the node content to list")
  def String path;

  @Option(name="-d",aliases=["--depth"],usage="Print depth")
  def Integer depth;

  public Object execute() throws ScriptException {
    assertConnected();

    //
    def node = path == null ? getCurrentNode() : findNodeByPath(path);

    //
    def builder = new DisplayBuilder();

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