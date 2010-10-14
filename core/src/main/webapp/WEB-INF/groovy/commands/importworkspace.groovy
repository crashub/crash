import java.io.InputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.Argument;
import org.crsh.command.ScriptException;
import org.crsh.command.Description;
import org.crsh.jcr.JCR;
import org.crsh.util.SubInputStream;
import javax.jcr.Node;
import javax.jcr.SimpleCredentials;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

@Description("Import a workspace from the file system (experimental)")
class workspaceimport extends org.crsh.command.ClassCommand {

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
    if (!file.exists()) {
      throw new ScriptException("File $file.absolutePath does not exist");
    }

    //
    def importSession;
    boolean close;
    if (session != null) {
      importSession = session;
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
      importSession = repo.login(credentials, workspaceName);
      close = true;
    }
    try {
      importRoot(importSession.rootNode, file);
      return "Imported $file.absolutePath in workspace $importSession.workspace.name";
    } finally {
      if (close)
        importSession.logout();
    }
  }

  private void importRoot(Node node, File file) {
    def InputStream input = new FileInputStream(file);
    input = new DataInputStream(input);
    try {

      // Skip create
      input.readBoolean();
      // Skip path
      input.readUTF();
      // Skip name
      input.readUTF();
      // Skip primaryNodeType
      input.readUTF();

      // Import state
      def importedNodes = [:];
      importedNodes["/"] = node;
      importNodeState(node, importedNodes, input);
    } finally {
      input.close();
    }
  }

  private Node importNode(Node parent, Map importedNodes, DataInputStream input) {

    // Should we created it or not
    def create = input.readBoolean();
    // Get path
    def path = input.readUTF();
    // Get name
    def name = input.readUTF();
    // Get primaryNodeType
    def primaryNodeType = input.readUTF();

    // Create node
    def child;
    if (create) {
      child = parent.addNode(name, primaryNodeType);
      System.out.println("added node $child.path");
    }
    else
      child = parent.getNode(name);

    // Add to map
    importedNodes[path] = child;

    // Import state
    importNodeState(child, importedNodes, input);

    //
    return node;
  }

  private void importNodeState(Node node, Map importedNodes, DataInputStream input) {
    // Add mixins
    def mixinCount = input.readInt();
    while (mixinCount-- > 0) {
      def mixinName = input.readUTF();
      node.addMixin(mixinName);
    }

    // Add children
    out:
    while (true) {
      switch (input.readInt()) {
        case 0:
          importNode(node, importedNodes, input);
          break;
        case 1:
          def propertyName = input.readUTF();
          def single = input.readBoolean();
          if (single) {
            def value = importPropertyValue(node, importedNodes, input);
            node.setProperty(propertyName, value);
          } else {
            int propertyCount = input.readInt();
            def values = [];
            while (propertyCount-- > 0) {
              def value = importPropertyValue(node, importedNodes, input);
              values.add(value);
            }
            node.setProperty(propertyName, values.toArray(new Value[values.size()]));
          }
          break;
        case 2:
          break out;
        default:
          throw new AssertionError("should not be here");
      }
    }
  }

  private Value importPropertyValue(Node node, Map importedNodes, DataInputStream input) {
    def type = input.readInt();
    switch (type) {
      case PropertyType.REFERENCE:
        def path = input.readUTF();
        def referencedNode = importedNodes[path];
        if (referencedNode == null) {
          referencedNode = importNode(node, importedNodes, input);
        }
      return node.session.valueFactory.createValue(referencedNode);
      case PropertyType.BINARY:
        long length = input.readLong();
        def stream = new SubInputStream(input, length);
        def out =  new ByteArrayOutputStream(length);
        out.withStream { out << stream };
        return node.session.valueFactory.createValue(new ByteArrayInputStream(out.toByteArray()));
        break;
      default:
        def stringValue = input.readUTF();
        return node.session.valueFactory.createValue(stringValue, type);
    }
  }
}
