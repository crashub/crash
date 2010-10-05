import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import org.crsh.command.ScriptException;
import org.crsh.command.Description;

@Description("Creates one or several nodes")
public class addnode extends org.crsh.command.ClassCommand {

  @Argument(required=true,index=0,usage="The paths of the new node to be created, the paths can either be absolute or relative.")
  def List<String> paths;

  @Option(name="-t",aliases=["--type"],usage="The name of the primary node type to create")
  def String primaryNodeTypeName;

  public Object execute() throws ScriptException {
    assertConnected();

    //
    def ret = 'Node';
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
      ret <<= " $node.path";
    }

    System.out.println("$ret created");
    System.out.println("$ret created");
    System.out.println("$ret created");

    //
    return "$ret created";
  }
}

