import org.crsh.cmdline.annotations.Usage
import org.crsh.cmdline.annotations.Argument
import org.crsh.cmdline.annotations.Command;

public class echo extends org.crsh.command.CRaSHCommand {

  @Command
  @Usage("echo text")
  public void main(@Usage("the content") @Argument List<String> arguments) throws ScriptException {
    arguments.each { out << it }
  }
}