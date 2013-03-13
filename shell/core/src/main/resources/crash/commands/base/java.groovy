import org.crsh.cli.Usage
import org.crsh.cli.Command
import org.crsh.cli.Argument
import org.crsh.cli.Required
import org.crsh.text.ui.UIBuilder

@Usage("various java language commands")
class java {
  @Usage("print information about a java type")
  @Command
  void type(@Usage("The full qualified type name") @Required @Argument String name) {
    try {
      Class clazz = Thread.currentThread().getContextClassLoader().loadClass(name)
      out << "$name:\n"

      // Interface hierarchy
      def hierarchy = new UIBuilder()
      this.hierarchy(hierarchy, clazz)
      out.println(hierarchy)

      //
      if (clazz.declaredFields.length > 0)
        out << "declared fields:\n"
      clazz.declaredFields.each() { field ->
        out << "$field.type: $field.name\n";
      }

    } catch (ClassNotFoundException e) {
      out << "Class $name was not found";
    };
  }

  private void hierarchy(UIBuilder builder, Class clazz) {
    builder.node(clazz.name) {
      if (clazz.superclass != null) {
        hierarchy(builder, clazz.superclass);
      }
      def interfaces = clazz.interfaces
      if (interfaces.length > 0) {
        node("interfaces") {
          interfaces.each() { itf ->
            hierarchy(builder, itf);
          }
        }
      }
    }
  }
}