import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.Workspace;

import org.crsh.command.ScriptException;
import org.kohsuke.args4j.Argument;
import org.crsh.command.Description;

@Description("move a node to another location")
public class mv extends org.crsh.command.ClassCommand {

  @Argument(required=true,index=0,usage="The path of the source node to move")
  def String source;

  @Argument(required=true,index=1,usage="The target path")
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

    sourceNode.session.workspace.move(srcPath, destPath);
  }
}