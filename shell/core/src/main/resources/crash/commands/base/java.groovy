import org.crsh.command.CRaSHCommand
import org.crsh.cmdline.annotations.Usage
import org.crsh.cmdline.annotations.Command
import org.crsh.cmdline.annotations.Argument
import org.crsh.cmdline.annotations.Required
import org.crsh.shell.ui.UIBuilder

@Usage("various java language commands")
class java extends CRaSHCommand {
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