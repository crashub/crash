import org.crsh.jcr.PropertyType;
import org.crsh.command.ScriptException;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import javax.jcr.Node;
import javax.jcr.Property;
import org.crsh.command.Description;
import org.exoplatform.services.jcr.access.AccessControlList;
import java.util.ArrayList;

@Description("Set the permissions on a JCR node, the mixing exo:privilegeable is added when it is not already present")
public class setperm extends org.crsh.command.ClassCommand {

  @Option(name="-n",aliases=["--node"],usage="The path of the node to alter, if not specified the current node is used")
  def String nodePath;

  @Argument(index = 0,metaVar="identity",required = true,usage="The identity to use")
  def String identity;

  @Argument(index = 1,usage = "A list of permissions among {read,add_node,set_property,remove}")
  List<String> arguments;

  public Object execute() throws ScriptException {
    def node = nodePath != null ? findNodeByPath(nodePath) : getCurrentNode();

    //
    if (!node.isNodeType("exo:privilegeable")) {
      node.addMixin("exo:privilegeable");
    }

    //
    AccessControlList acl = node.ACL;
    def permissions = acl.getPermissions(identity) ?: [];

    //
    arguments.each {
      // Handle permission modifier
      boolean add = true;
      if (it.charAt(0) == '!') {
        add = false;
        it = it.substring(1);
      } else if (it.charAt(0) == '+') {
        it = it.substring(1);
      }
      if (add) {
        if (!permissions.contains(it)) {
          permissions.add(it);
        }
      } else {
        permissions.remove(it);
      }
    };

    //
    node.setPermission(identity, permissions.toArray(new String[permissions.size()]));

    //
    return "Node $node.path updated to $permissions";
  }
}
