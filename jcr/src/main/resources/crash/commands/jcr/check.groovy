import org.crsh.jcr.command.PathArg;

public class check extends org.crsh.jcr.command.JCRCommand {
  // The path of the node to checkin
  @Description(display = "Checkin a node")
  @Command
  public void IN(@PathArg String path) throws ScriptException {
    assertConnected();
    def node = findNodeByPath(path);
    node.checkin();
  }

  // The path of the node to checkout
  @Description(display = "Checkout a node")
  @Command
  public void OUT(@PathArg String path) throws ScriptException {
    assertConnected();
    def node = findNodeByPath(path);
    node.checkout();
  }
}
