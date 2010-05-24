import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.util.Calendar;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.Argument;
import org.crsh.command.ScriptException;
import org.crsh.command.Description;

@Description("Export a node to an nt file")
class exportnode extends org.crsh.command.ClassCommand {

  @Argument(required=true,index=0,usage="Path of the node to export")
  def String srcPath;

  @Argument(required=true,index=1,usage="Path of the created nt:file node containing the exported content")
  def String dstPath;

  public Object execute() throws ScriptException {
    assertConnected();

    // Source node to export
    def srcNode = findNodeByPath(srcPath);

    //
    def session = srcNode.session;

    // Destination parent
    int pos = dstPath.lastIndexOf('/');
    if (pos == -1)
      throw new ScriptException("The destination must be absolute");
    def dstParenNodet;
    def dstName;
    if (pos == 0) {
      dstParentNode = findNodeByPath("/");
      dstName = dstPath.substring(1);
    } else {
      dstParentNode = findNodeByPath(dstPath.substring(0, pos));
      dstName = dstPath.substring(pos + 1);
    }

    //
    if (dstParentNode[dstName] != null) {
      throw new ScriptException("Destination path already exist");
    }

    // Export
    def baos = new ByteArrayOutputStream();
    srcNode.session.exportSystemView(srcNode.path, baos, false, false);
    baos.close();

    // Create nt file / nt resource
    def fileNode = dstParentNode.addNode(dstName, "nt:file");
    def res = fileNode.addNode("jcr:content", "nt:resource");
    def bais = new ByteArrayInputStream(baos.toByteArray());
    res.setProperty("jcr:mimeType", "application/xml");
    res.setProperty("jcr:data", bais);
    res.setProperty("jcr:lastModified", Calendar.getInstance());

    //
    return "The node has been exported";
  }
}
