import org.crsh.jcr.command.PathArg;
import javax.jcr.ImportUUIDBehavior
import org.crsh.cmdline.annotations.Usage
import org.crsh.cmdline.annotations.Man
import org.crsh.cmdline.annotations.Command
import org.crsh.command.InvocationContext
import org.crsh.cmdline.annotations.Option
import org.crsh.cmdline.annotations.Required;

public class node extends org.crsh.jcr.command.JCRCommand {

  @Usage("creates one or several nodes")
  @Man("""\
The addnode command creates one or several nodes. The command takes at least one node as argument, but it can
 take more. Each path can be either absolute or relative, relative path creates nodes relative to the current node.
 By default the node type is the default repository node type, but the option -t can be used to specify another one.

[/registry]% addnode foo
Node /foo created

[/registry]% addnode -t nt:file bar juu
Node /bar /juu created

The addnode command is a <Void,Node> command that produces all the nodes that were created.""")
  @Command
  public void add(
    InvocationContext<Void, Node> context,
    @Usage("the paths to be created")
    @Man("The paths of the new node to be created, the paths can either be absolute or relative.")
    @PathArg List<String> paths,
    @Usage("the node type name")
    @Man("The name of the primary node type to create.")
    @Option(names=["t","type"]) String primaryNodeTypeName)
    throws ScriptException {
    assertConnected();

    //
    context.writer <<= 'Node';
    paths.each {

      def parent;
      if (it.charAt(0) == '/') {
        parent = session.rootNode;
        it = it.substring(1);
      } else {
        parent = getCurrentNode();
      }

      //
      def node;
      if (primaryNodeTypeName != null) {
        node = parent.addNode(it, primaryNodeTypeName);
      } else {
        node = parent.addNode(it);
      }

      //
      context.produce(node);

      //
      context.writer <<= " $node.path";
    }

    //
    context.writer <<= " created";
  }

  @Command
  @Man("""\
Exports a node as an nt file in the same workspace:

[/]% node export gadgets /gadgets.xml
The node has been exported
""")
  @Usage("export a node to an nt file")
  public Object export(
    @Required @PathArg @Usage("path of the exported node") String src,
    @Required @PathArg @Usage("path of the exported nt:file node") String dst) throws ScriptException {

    //
    assertConnected();

    // Source node to export
    def srcNode = findNodeByPath(src);

    //
    def session = srcNode.session;

    // Destination parent
    int pos = dst.lastIndexOf('/');
    if (pos == -1)
      throw new ScriptException("The destination must be absolute");
    def dstParenNodet;
    def dstName;
    if (pos == 0) {
      dstParentNode = findNodeByPath("/");
      dstName = dst.substring(1);
    } else {
      dstParentNode = findNodeByPath(dst.substring(0, pos));
      dstName = dst.substring(pos + 1);
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

  @Command
  @Usage("imports a node from an nt file")
  @Man("""\
Imports a node from an nt:file node located in the workspace:

[/]% importnode /gadgets.xml /
Node imported
""")
  public Object IMPORT(
    @Required @PathArg @Usage("path of the imported nt:file node") String src,
    @Required @PathArg @Usage("path of the parent imported node") String dst) throws ScriptException {

    //
    assertConnected();

    // Source node to export
    def srcNode = findNodeByPath(src);

    //
    if (srcNode.primaryNodeType.name != "nt:file")
      throw new ScriptException("Can only import file");

    // Get content
    def data = srcNode["jcr:content"]["jcr:data"];

    //
    def dstNode = findNodeByPath(dst);

    //
    srcNode.session.importXML(dst, data, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);

    //
    return "Node imported";
  }
}