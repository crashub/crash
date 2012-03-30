import org.crsh.command.ScriptException;
import javax.jcr.Node;
import org.exoplatform.services.jcr.access.AccessControlList;
import org.crsh.command.InvocationContext
import org.crsh.cmdline.annotations.Command
import org.crsh.cmdline.annotations.Usage
import org.crsh.cmdline.annotations.Man
import org.crsh.jcr.command.Path
import org.crsh.cmdline.annotations.Option
import org.crsh.cmdline.annotations.Argument;

public class setperm extends org.crsh.jcr.command.JCRCommand {

  @Option(names = ["i","identity"]) @Usage("the identity") def String identity;
  @Option(names = ["a","add"]) @Usage("the permissions to use") def List<String> toAdd;
  @Option(names = ["r","remove"]) @Usage("the permissions to remove") def List<String> toRemove;

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

  @Command
  @Usage("modify the security permissions of a JCR node")
  @Man("""The setperm commands configures the security of a node based on (see eXo JCR access control at
http://wiki.exoplatform.com/xwiki/bin/view/JCR/Access%20Control). When a node is protected by access control, it owns a
mixin named exo:privilegeable that contains a exo:permissions property, for instance:

[/production]% ls
/production
+-properties
| +-jcr:primaryType: nt:unstructured
| +-jcr:mixinTypes: [exo:privilegeable]
| +-exo:permissions: [*:/platform/administrators read,*:/platform/administrators add_node,*:/platform/administrators set_property,*:/platform/administrators remove]
+-children
| +-/production/app:gadgets
| +-/production/app:applications
| +-/production/mop:workspace

You can alter the node permission list with the setperm command:

[/production]% setperm -i *:/platform/mygroup -a read -a add_node /
Node /production updated to [read,add_node]

You can also remove a permission by using the -r option.

[/production]% setperm -i *:/platform/mygroup -r add_node /
Node /production updated to [read]

The setperm command will add automatically the exo:privilegeable mixin on the node when it is missing. The setperm is
a <Node,Void> command altering the security of the consumed node stream.""")
  public void main(InvocationContext<Node, Node> context,
    @Argument @Usage("the paths to secure") @Man("The node path list to secure") List<Path> paths) throws ScriptException {

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
