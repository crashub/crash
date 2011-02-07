import org.crsh.command.DescriptionMode
import org.crsh.command.CRaSHCommand
import org.crsh.cmdline.annotations.Usage
import org.crsh.cmdline.annotations.Command;

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
    shellContext.listResourceId(org.crsh.plugin.ResourceKind.SCRIPT).each() {
      String name ->
      try {
        def cmd = shell.getCommand(name);
        if (cmd != null) {
          def desc = cmd.describe(name, DescriptionMode.DESCRIBE) ?: "";
          names.add(name);
          descs.add(desc);
          len = Math.max(len, name.length());
        }
      } catch (org.crsh.shell.impl.CreateCommandException ignore) {
        //
      }
    }

    //
    def ret = "Try one of these commands with the -h or --help switch:\n\n";
    for (int i = 0;i < names.size();i++) {
      def name = names[i];
      char[] chars = new char[TAB.length() + len - name.length()];
      Arrays.fill(chars, (char)' ');
      def space = new String(chars);
      ret += "$TAB$name$space${descs[i]}\n";
    }
    ret += "\n";
    return ret;
  }
}