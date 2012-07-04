import org.crsh.command.DescriptionFormat
import org.crsh.command.CRaSHCommand
import org.crsh.cmdline.annotations.Usage
import org.crsh.cmdline.annotations.Command
import org.crsh.shell.ui.UIBuilder

class help extends CRaSHCommand
{

  /** . */
  private static final String TAB = "  ";

  @Usage("provides basic help")
  @Command
  Object main() {
    def names = [];
    def descs = [];
    int len = 0;
    crash.context.listResourceId(org.crsh.plugin.ResourceKind.COMMAND).each() {
      String name ->
      try {
        def cmd = crash.getCommand(name);
        if (cmd != null) {
          def desc = cmd.describe(name, DescriptionFormat.DESCRIBE) ?: "";
          names.add(name);
          descs.add(desc);
          len = Math.max(len, name.length());
        }
      } catch (Exception ignore) {
        //
      }
    }
    
    def builder = new UIBuilder()

    //
    builder.label("Try one of these commands with the -h or --help switch:\n\n");

    builder.table() {
      row(decoration: bold, foreground: black, background: white) {
        label("NAME"); label("DESCRIPTION")
      }
      for (int i = 0;i < names.size();i++) {
        row() {
            label(value: names[i], foreground: red); label(descs[i])
        }
      }
    }
    
    return builder;
  }
}