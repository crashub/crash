import org.crsh.jcr.command.Path
import org.crsh.cmdline.annotations.Usage
import org.crsh.cmdline.annotations.Command
import org.crsh.cmdline.annotations.Argument
import org.crsh.cmdline.annotations.Man

@Usage("versioning commands")
@Man("Versionning commands")
public class version extends org.crsh.jcr.command.JCRCommand {
  // The path of the node to checkin
  @Usage("checkin a node")
  @Man("Perform a node checkin")
  @Command
  public void checkin(@Argument @Usage("the path to checkin") @Man("The node path to checkin") Path path) throws ScriptException {
    assertConnected();
    def node = findNodeByPath(path);
    node.checkin();
  }

  // The path of the node to checkout
  @Usage("checkout a node")
  @Man("Perform a node checkout")
  @Command
  public void checkout(@Argument @Usage("the path to checkout") @Man("The node path to checkout") Path path) throws ScriptException {
    assertConnected();
    def node = findNodeByPath(path);
    node.checkout();
  }
}
