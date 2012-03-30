import org.crsh.command.ScriptException;
import org.crsh.cmdline.annotations.Man
import org.crsh.cmdline.annotations.Command
import org.crsh.cmdline.annotations.Usage
import org.crsh.jcr.command.Path
import org.crsh.cmdline.annotations.Argument;

public class commit extends org.crsh.jcr.command.JCRCommand {

  @Usage("saves changes")
  @Command
  @Man("""Saves the changes done to the current session. A node can be provided to save the state of the
this nodes and its descendants only.""")
  public void main(
    @Argument
    @Usage("the path to commit")
    @Man("The path of the node to commit") Path path) throws ScriptException {
    assertConnected();
    def node = findNodeByPath(path);
    node.save();
  }
}
