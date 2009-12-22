import org.crsh.jcr.PropertyType;
import org.crsh.shell.ScriptException;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import javax.jcr.Node;
import javax.jcr.Property;
import org.crsh.shell.Description;

@Description("Updates a property of a node")
public class set extends org.crsh.shell.ClassCommand {

  @Argument(required=false,index=0,usage="The path of the property to alter")
  def String propertyPath;

  @Argument(required=false,index=1,usage="The new value of the property")
  def String propertyValue;

  @Option(name="-t",aliases=["--type"],usage="The property type to use when the property does not exist")
  def PropertyType propertyType = PropertyType.STRING;

  public Object execute() throws ScriptException {
    assertConnected();

    //
    def pos = propertyPath.lastIndexOf("/");
    def propertyName = propertyPath.substring(pos + 1);
    def parentPath = pos == - 1 ? "." : propertyPath.substring(0, pos + 1);

    // Get the parent node
    def parent = findNodeByPath(parentPath);

    // Update the property
    if (parent.hasProperty(propertyName)) {
      parent[propertyName] = propertyValue;
      return "Property updated";
    } else {
      if (propertyValue != null) {
        // Try to find some meta for the value type first
        def requiredType = propertyType.value;
        for (def pd : parent.primaryNodeType.propertyDefinitions) {
          if (pd.name == propertyName) {
            type = pd.requiredType;
            break;
          }
        }

        parent.setProperty(propertyName, propertyValue, requiredType);
        return "Property created";
      } else {
        return "No property updated";
      }
    }
  }
}
