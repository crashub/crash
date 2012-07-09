import org.crsh.command.CRaSHCommand
import org.crsh.cmdline.annotations.Usage
import org.crsh.cmdline.annotations.Command

class env extends CRaSHCommand
{
  @Usage("display the term env")
  @Command
  Object main() {
    StringBuilder sb = new StringBuilder();
    sb.append("width: $context.width");
    return sb;
  }
}