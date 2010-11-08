import org.crsh.jcr.PropertyType;
import org.crsh.command.ScriptException;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import javax.jcr.Node;
import javax.jcr.Property;
import org.crsh.command.Description;
import org.crsh.command.CommandContext;

@Description("""Updates a property of the current node. When the command is piped it sets the property\
 of the consumed nodes.""")
public class set extends org.crsh.command.BaseCommand<Node, Void> {

  @Argument(required=false,index=0,usage="The name of the property to alter")
  def String propertyName;

  @Argument(required=false,index=1,usage="The new value of the property")
  def String propertyValue;

  @Option(name="-t",aliases=["--type"],usage="The property type to use when the property does not exist")
  def PropertyType propertyType = PropertyType.STRING;

  public void execute(CommandContext<Node, Void> context) throws ScriptException {
    if (context.piped) {
      context.consume().each { node ->
        update(node);
      }
    } else {
      // Get the current node
      def node = getCurrentNode();
      update(node);
    }
  }

  private void update(Node node) {
    // Set the property
    if (node.hasProperty(propertyName)) {
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
            type = pd.requiredType;
            break;
          }
        }

        // Perform the set
        node.setProperty(propertyName, propertyValue, requiredType);
      } else {
        // Remove any existing property with that name
        node[propertyName] = null;
      }
    }
  }
}
