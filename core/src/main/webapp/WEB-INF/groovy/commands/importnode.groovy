import org.crsh.command.ScriptException;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.util.Calendar;
import javax.jcr.ImportUUIDBehavior;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.Argument;
import org.crsh.command.Description;

@Description("Import a node from an nt file")
public class importnode extends org.crsh.command.ClassCommand {

  @Argument(required=true,index=0,usage="Path of the nt:file containing the content to import")
  def String srcPath;

  @Argument(required=true,index=1,usage="Path of the parent node that will contain the imported content")
  def String dstPath;

  public Object execute() throws ScriptException {
    assertConnected();
  
    // Source node to export
    def srcNode = findNodeByPath(srcPath);

    //
    if (srcNode.primaryNodeType.name != "nt:file")
      throw new ScriptException("Can only import file");

    // Get content
    def data = srcNode["jcr:content"]["jcr:data"];

    //
    def dstNode = findNodeByPath(dstPath);

    //
    srcNode.session.importXML(dstPath, data, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);

    //
    return "Node imported";
  }
}
