import org.crsh.command.CRaSHCommand
import org.crsh.cmdline.annotations.Usage
import org.crsh.cmdline.annotations.Command
import org.crsh.command.InvocationContext

class env extends CRaSHCommand
{
  @Usage("display the term env")
  @Command
  Object main(InvocationContext<Void, Void> context) throws ScriptException {
    StringBuilder sb = new StringBuilder();
    sb.append("width: $context.width");
    return sb;
  }
}