import org.crsh.command.DescriptionFormat
import org.crsh.cli.Usage
import org.crsh.cli.Command
import org.crsh.text.ui.UIBuilder
import org.crsh.plugin.ResourceKind;

class help
{

  /** . */
  private static final String TAB = "  ";

  @Usage("provides basic help")
  @Command
  Object main() {
    def names = [];
    def descs = [];
    int len = 0;
    crash.commandNames.each() {
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
    builder.label("Try one of these commands with the -h or --help switch:\n");

    builder.table(rightCellPadding: 1) {
      row(bold: true, fg: black, bg: white) {
        label("NAME"); label("DESCRIPTION")
      }
      for (int i = 0;i < names.size();i++) {
        row() {
            label(foreground: red, names[i]); label(descs[i])
        }
      }
    }
    
    return builder;
  }
}