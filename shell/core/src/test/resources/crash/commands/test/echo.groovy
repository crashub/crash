import org.crsh.command.InvocationContext
import org.crsh.cmdline.annotations.Usage
import org.crsh.cmdline.annotations.Argument
import org.crsh.cmdline.annotations.Command;

public class echo extends org.crsh.command.CRaSHCommand {

  @Command
  @Usage("echo text")
  public void main(
    InvocationContext context,
    @Usage("the content") @Argument
    List<String> arguments) throws ScriptException {
    arguments.each { context.writer.print(it) }
  }
}