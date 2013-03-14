import org.crsh.cli.Usage
import org.crsh.cli.Command

class env
{
  @Usage("display the term env")
  @Command
  void main() {
    out << "width: $context.width\n"
    out << "height: $context.height\n"
    out << "session: ${context.session.keySet()}\n"
    out << "attributes: ${context.attributes.keySet()}\n"
 }
}