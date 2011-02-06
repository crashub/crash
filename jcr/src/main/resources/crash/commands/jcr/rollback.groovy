import org.crsh.command.ScriptException;
import org.crsh.cmdline.annotations.Usage
import org.crsh.jcr.command.PathArg
import org.crsh.cmdline.annotations.Man
import org.crsh.cmdline.annotations.Command;

public class rollback extends org.crsh.jcr.command.JCRCommand {

  @Usage("rollback changes")
  @Command
  @Man("""Rollbacks the changes of the current session. A node can be provided to rollback the state of the
this nodes and its descendants only.""")
  public void main(@PathArg @Usage("the path of the node to commit") String path) throws ScriptException {
    assertConnected();
    def node = findNodeByPath(path);
    node.refresh(false);
  }
}
