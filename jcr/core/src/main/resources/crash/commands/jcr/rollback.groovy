import org.crsh.command.ScriptException;
import org.crsh.cmdline.annotations.Usage
import org.crsh.jcr.command.PathArg
import org.crsh.cmdline.annotations.Man
import org.crsh.cmdline.annotations.Command
import org.crsh.cmdline.annotations.Argument
import org.crsh.jcr.command.Path;

public class rollback extends org.crsh.jcr.command.JCRCommand {

  @Usage("rollback changes")
  @Command
  @Man("""Rollbacks the changes of the current session. A node can be provided to rollback the state of the
this nodes and its descendants only.""")
  public void main(@Argument @Man("the path to rollback") @Usage("The path of the node to rollback") Path path) throws ScriptException {
    assertConnected();
    def node = findNodeByPath(path);
    node.refresh(false);
  }
}
