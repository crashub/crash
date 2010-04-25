import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.Workspace;

import org.crsh.shell.ScriptException;
import org.kohsuke.args4j.Argument;
import org.crsh.shell.Description;

@Description("copy a node to another")
public class cp extends org.crsh.shell.ClassCommand {

  @Argument(required=true,index=0,usage="The path of the source node to copy")
  def String source;

  @Argument(required=true,index=1,usage="The path of the target node to be copied")
  def String target;

  public Object execute() throws ScriptException {
    assertConnected()


    def sourceNode = findItemByPath(source)
    def srcPath = sourceNode.path
	  def destPath = target

	  // convert relative path to absolute
	  if (!target.startsWith("/")) {
		  def sep = (currentPath.equals("/")) ? "" : "/"
		  destPath = currentPath + sep + target
	  }

    sourceNode.session.workspace.copy(srcPath, destPath)
  }
}