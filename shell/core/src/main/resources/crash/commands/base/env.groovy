import org.crsh.command.CRaSHCommand
import org.crsh.cmdline.annotations.Usage
import org.crsh.cmdline.annotations.Command

class env extends CRaSHCommand
{
  @Usage("display the term env")
  @Command
  void main() {
    out << "width: $context.width\n"
    out << "session: $context.session\n"
    out << "attributes: $context.attributes\n"
 }
}