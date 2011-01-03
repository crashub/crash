import org.crsh.jcr.command.PathArg;
import org.crsh.shell.ui.UIBuilder;

public class ls extends org.crsh.jcr.command.JCRCommand {

  @Command(description = "List the content of a node")
  public Object main(
  // Path of the node content to list
  @PathArg String path,
  @Option(names=["d","depth"],description="Print depth") Integer depth) throws ScriptException {
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