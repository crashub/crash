import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.Workspace;

import java.util.List;

import org.kohsuke.args4j.Argument;
import org.crsh.command.ScriptException;
import org.crsh.command.Description;

@Description("Copy a node to another")
public class cp extends org.crsh.command.ClassCommand {

  @Argument(required=true,metaVar="source path",index=0,usage="The path of the source node to copy")
  def String source;

  @Argument(required=true,metaVar="target path",index=1,usage="The path of the target node to be copied")
  def String target;

  public Object execute() throws ScriptException {
    assertConnected();

    //
    def sourceNode = findItemByPath(source);

    //
    def targetPath = absolutePath(target);

    //
    sourceNode.session.workspace.copy(sourceNode.path, targetPath);
  }
}