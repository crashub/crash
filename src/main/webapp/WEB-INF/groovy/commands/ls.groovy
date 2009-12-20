import org.crsh.console.ConsoleBuilder;
import org.kohsuke.args4j.Argument;

public class ls extends org.crsh.shell.ClassCommand {

  @Argument(required=false,index=0,usage="Path of the node content to list")
  def String path;

  public Object execute() throws ScriptException {
    assertConnected();

    //
    def node = path == null ? getCurrentNode() : findNodeByPath(path);

    //
    def properties = node.getProperties();
    def children = node.getNodes();

    //
    def builder = new ConsoleBuilder();

    //
    builder.table {
      node.eachProperty { property ->
        row([property.name, " ", formatPropertyValue(property)])
      }
    };

    //
    builder.table {
      node.each { child ->
        row([child.name])
      }
    };

    //
    return builder;
  }
}