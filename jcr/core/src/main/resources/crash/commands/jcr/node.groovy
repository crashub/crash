import javax.jcr.ImportUUIDBehavior
import javax.jcr.Node;
import org.crsh.cli.Usage
import org.crsh.cli.Man
import org.crsh.cli.Command
import org.crsh.command.InvocationContext
import org.crsh.cli.Option
import org.crsh.cli.Required;
import org.crsh.jcr.PropertyType
import org.crsh.cli.Argument
import org.crsh.jcr.command.Path
import org.crsh.command.PipeCommand

@Usage("node commands")
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
    InvocationContext<Node> context,
    @Usage("the paths to be created")
    @Man("The paths of the new node to be created, the paths can either be absolute or relative.")
    @Argument List<Path> paths,
    @Usage("the node type name")
    @Man("The name of the primary node type to create.")
    @Option(names=["t","type"]) String primaryNodeTypeName) {
    assertConnected();

    //
    out << 'Node';
    paths.each {

      def parent;
      if (it.isAbsolute()) {
        parent = session.rootNode;
        it = new Path(it.value.substring(1));
      } else {
        parent = getCurrentNode();
      }

      //
      def node;
      if (primaryNodeTypeName != null) {
        node = parent.addNode(it.value, primaryNodeTypeName);
      } else {
        node = parent.addNode(it.value);
      }

      //
      context.provide(node);

      //
      out << " $node.path";
    }

    //
    out << " created";
  }

  @Command
  @Usage("set a property on the current node")
  @Man("""\
The set command updates the property of a node.

Create or destroy property foo with the value bar on the root node:

[/]% set foo bar
Property created

Update the existing foo property:

[/]% set foo juu

When a property is created and does not have a property descriptor that constraint its type, you can specify it
with the -t option

[/]% set -t LONG long_property 3

Remove a property

[/]% set foo

set is a <Node,Void> command updating the property of the consumed node stream.""")
  public PipeCommand<Node, Node> set(
    @Argument @Usage("the property name") @Man("The name of the property to alter") String propertyName,
    @Argument @Usage("the property value") @Man("The new value of the property") String propertyValue,
    @Option(names=["t","type"]) @Usage("the property type") @Man("The property type to use when it cannot be inferred") PropertyType propertyType) {
    propertyType = propertyType ?: PropertyType.STRING;
    return new PipeCommand<Node, Node>() {
      @Override
      void open() {
        if (!isPiped()) {
          // Operate on current node
          def node = getCurrentNode();
          update(node, propertyName, propertyValue, propertyType);
        }
      }

      @Override
      void provide(Node node) {
        update(node, propertyName, propertyValue, propertyType);
        context.provide(node)
      }
    };
  }

  private void update(Node node, String propertyName, String propertyValue, PropertyType propertyType) {
    // Set the property
    if (propertyValue != null && node.hasProperty(propertyName)) {
      // If the current node has already a property, we just update it
      node[propertyName] = propertyValue;
    } else {

      // Otherwise we try to create it
      if (propertyValue != null) {
        // Use the specified property type (or STRING if none was set explicitely)
        def requiredType = propertyType.value;

        // But if we can find meta info about it we use it
        for (def pd : node.primaryNodeType.propertyDefinitions) {
          if (pd.name == propertyName) {
            requiredType = pd.requiredType;
            break;
          }
        }

        // Perform the set
        node.setProperty(propertyName, propertyValue, requiredType);
      } else {
        // Remove any existing property with that name
        if (node.hasProperty(propertyName)) {
          node.getProperty(propertyName).remove()
        }
      }
    }
  }

  @Command
  @Man("""\
Exports a node as an nt file in the same workspace:

[/]% node export gadgets /gadgets.xml
The node has been exported
""")
  @Usage("export a node to an nt file")
  public Object export(
    @Required @Argument @Usage("the source path") @Man("The path of the exported node") Path source,
    @Required @Argument @Usage("the target path") @Man("The path of the exported nt:file node") Path target) {

    //
    assertConnected();

    // Source node to export
    def srcNode = findNodeByPath(source);

    //
    def session = srcNode.session;

    // Destination parent
    int pos = target.value.lastIndexOf('/');
    if (pos == -1)
      throw new ScriptException("The destination must be absolute");
    def dstParenNodet;
    def dstName;
    if (pos == 0) {
      dstParentNode = findNodeByPath(Path.ROOT);
      dstName = target.value.substring(1);
    } else {
      dstParentNode = findNodeByPath(new Path(target.value.substring(0, pos)));
      dstName = target.value.substring(pos + 1);
    }

    //
    if (dstParentNode.hasNode(dstName)) {
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
    @Required @Argument @Usage("the source path") @Man("The path of the imported nt:file node") Path source,
    @Required @Argument @Usage("the target path")  @Man("The path of the parent imported node") Path target) {

    //
    assertConnected();

    // Source node to export
    def srcNode = findNodeByPath(source);

    //
    if (srcNode.primaryNodeType.name != "nt:file")
      throw new ScriptException("Can only import file");

    // Get content
    def data;
    if(srcNode.hasNode("jcr:content") && srcNode.getNode("jcr:content").hasProperty("jcr:data")){
      data = srcNode.getNode("jcr:content").getProperty("jcr:data").getStream();
    } else {
      throw new ScriptException("The source file " + source + " is broken");
    }

    //
    def dstNode = findNodeByPath(target);

    //
    srcNode.session.importXML(target.value, data, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);

    //
    return "Node imported";
  }
}