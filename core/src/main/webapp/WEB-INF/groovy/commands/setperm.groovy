import org.crsh.jcr.PropertyType;
import org.crsh.command.ScriptException;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import javax.jcr.Node;
import javax.jcr.Property;
import org.crsh.command.Description;
import org.exoplatform.services.jcr.access.AccessControlList;
import java.util.ArrayList;
import org.crsh.command.CommandContext;

@Description("Set the permissions on a JCR node, the mixing exo:privilegeable is added when it is not already present")
public class setperm extends org.crsh.command.BaseCommand<Node, Node> {

  @Argument(index = 0,metaVar="paths",usage="The paths to alter security")
  def List<String> paths;

  @Option(name="-i",aliases=["--identity"],usage="The identity to use")
  def String identity;

  @Option(name="-a",aliases=["--add"],usage="The permission to add")
  List<String> toAdd;

  @Option(name="-r",aliases=["--remove"],usage="The permission to remove")
  List<String> toRemove;

  /**
   * Set permission on a single node.
   */
  private void updateperm(Node node) {
    // Maybe that should be configurable
    if (!node.isNodeType("exo:privilegeable")) {
      node.addMixin("exo:privilegeable");
    }

    //
    AccessControlList acl = node.ACL;
    def permissions = acl.getPermissions(identity) ?: [];

    //
    toAdd.each { perm ->
      if (!permissions.contains(perm)) {
        permissions.add(perm);
      }
    }

    //
    toRemove.each { perm ->
      permissions.remove(perm);
    }

    //
    node.setPermission(identity, permissions.toArray(new String[permissions.size()]));
  }

  public void execute(CommandContext<Node, Node> context) throws ScriptException {

    //
    context.writer <<= "Updates permissions of nodes";

    //
    if (context.piped) {
      if (paths != null && paths.empty) {
        throw new ScriptException("No path arguments are permitted in a pipe");
      }

      // Node stream
      context.consume().each { node ->
        updateperm(node);
        context.produce(node);
        context.writer <<= " $node.path";
      }
    } else {
      // Node arguments
      paths.each { path ->
        def node = getNodeByPath(path);
        updateperm(node);
        context.produce(node);
        context.writer <<= " $node.path";
      }
    }
  }
}
