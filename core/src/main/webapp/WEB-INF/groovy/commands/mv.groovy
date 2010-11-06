import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.Workspace;

import org.crsh.command.ScriptException;
import org.kohsuke.args4j.Argument;
import org.crsh.command.Description;
import org.crsh.command.CommandContext;

@Description("move a node to another location")
public class mv extends org.crsh.command.BaseCommand<Void, Void> {

  @Argument(required=true,index=0,usage="The path of the source node to move")
  def String source;

  @Argument(required=true,index=1,usage="The destination path")
  def String target;

  public void execute(CommandContext<Node, Void> context) throws ScriptException {
    assertConnected()

    //
    def sourceNode = findNodeByPath(source);

    //
    def targetPath = absolutePath(target);

    // Perform move
    sourceNode.session.workspace.move(sourceNode.path, targetPath);
  }
}