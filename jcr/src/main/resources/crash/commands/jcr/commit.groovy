import org.kohsuke.args4j.Argument;
import org.crsh.command.ScriptException;
import org.crsh.command.Description
import org.crsh.cmdline.annotations.Man
import org.crsh.cmdline.annotations.Command
import org.crsh.cmdline.annotations.Usage
import org.crsh.jcr.command.PathArg;

public class commit extends org.crsh.jcr.command.JCRCommand {

  @Usage("saves changes")
  @Command
  @Man("""Saves the changes done to the current session. A node can be provided to save the state of the
this nodes and its descendants only.""")
  public void main(@PathArg @Usage("the path of the node to commit") String path) throws ScriptException {
    assertConnected();
    def node = findNodeByPath(path);
    node.save();
  }
}
