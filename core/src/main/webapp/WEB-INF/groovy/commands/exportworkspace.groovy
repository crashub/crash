import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.Argument;
import org.crsh.command.ScriptException;
import org.crsh.command.Description;
import org.crsh.jcr.JCR;
import javax.jcr.Node;
import javax.jcr.SimpleCredentials;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.ItemNotFoundException;

@Description("Export a workspace on the file system (experimental)")
class exportworkspace extends org.crsh.command.ClassCommand {

  @Option(required=false,name="-u",aliases=["--username"],usage="user name")
  def String userName;

  @Option(name="-p",required=false,aliases=["--password"],usage="password")
  def String password;

  @Option(name="-c",aliases=["--container"],usage="portal container name (eXo portal specific)")
  def String containerName;

  @Option(name="-w",aliases=["--workspace"],usage="the workspace name")
  def String workspaceName;

  @Argument(required=true,index=0,usage="The export file name")
  def String exportFileName;

  public Object execute() throws ScriptException {

    //
    File file = new File(exportFileName);
    if (file.exists()) {
      throw new ScriptException("File $file.absolutePath already exist");
    }

    //
    def exportSession;
    boolean close;
    if (session != null) {
      exportSession = session;
      close = false;
    } else {
      // Obtain the repository
      def properties = containerName == null ? [:] : ["exo.container.name":containerName];
      def repo = JCR.getRepository(properties);

      //
      if (userName == null) {
        throw new ScriptException("No username provided");
      }
      if (password == null) {
        throw new ScriptException("No password provided");
      }
      if (workspaceName == null) {
        throw new ScriptException("No workspace name provided");
      }

      // Get credentials
      def credentials = new SimpleCredentials(userName, password.toCharArray());

      // Get session
      exportSession = repo.login(credentials, workspaceName);
      close = true;
    }
    try {
      exportNode(exportSession.rootNode, file);
      return "Exported workspace $exportSession.workspace.name to $file.absolutePath";
    } finally {
      if (close)
        exportSession.logout();
    }
  }

  private void exportNode(Node node, File file) {
    def OutputStream out = new DataOutputStream(new FileOutputStream(file));
    try {
      exportNode(node, [] as Set, out);
    } finally {
      out.close();
    }
  }

  private void exportNode(Node node, Set exportedNodes, DataOutputStream out) {
    if (!exportedNodes.contains(node.path)) {
      exportedNodes.add(node.path);

      // True if the node should be created
      out.writeBoolean(node.path != "/" && !node.definition.autoCreated);

      //
      out.writeUTF(node.path);
      out.writeUTF(node.name);
      out.writeUTF(node.primaryNodeType.name);
      out.writeInt(node.mixinNodeTypes.length);
      node.mixinNodeTypes.each { mixinNodeType ->
        out.writeUTF(mixinNodeType.name);
      }

      //
      node.each { child ->
        // 0 means child node
        out.writeInt(0);
        exportNode(child, exportedNodes, out);
      }
      node.eachProperty { property ->
        exportProperty(property, exportedNodes, out);
      }

      // 2 means we are done with children
      out.writeInt(2);
    }
  }

  private void exportProperty(Property property, Set exportedNodes, DataOutputStream out) {
    if (property == null) {
      throw new NullPointerException("No null property accepted");
    }
    if (!property.definition.protected) {

      // 1 means child property
      out.writeInt(1);
      out.writeUTF(property.name);

      //
      try {
        def value = property.value;
        // True means single valued property
        out.writeBoolean(true);
        exportValue(property, value, exportedNodes, out);
      } catch (ValueFormatException e) {
        // False means multi valued property
        out.writeBoolean(false);
        out.writeInt(property.values.length);
        property.values.each { value ->
          exportValue(property, value, exportedNodes, out);
        };
      }
    }
  }

  private void exportValue(Property property, Value value, Set exportedNodes, DataOutputStream out) {
    if (value == null) {
      throw new NullPointerException("No null value accepted");
    }
    switch (value.type) {
      case PropertyType.REFERENCE:
        try {
          def referenced = property.getNode();
          System.out.println(referenced);
          out.writeInt(value.type);
          out.writeUTF(referenced.path);
          exportNode(referenced, exportedNodes, out);
        } catch (ItemNotFoundException e) {
        }
        break;
      case PropertyType.BINARY:
        out.writeInt(value.type);
        out.writeLong(property.length);
        def stream = value.stream;
        stream.withStream { out << stream };
        break;
      default:
        out.writeInt(value.type);
        out.writeUTF(value.string);
        break;
    }
  }
}
