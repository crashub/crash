import org.crsh.command.Description;
import org.crsh.command.ScriptException;
import org.crsh.cmdline.annotations.Usage
import org.crsh.cmdline.annotations.Argument
import org.crsh.cmdline.annotations.Command;

@Description("Invoke a static method")
public class wait extends org.crsh.command.CRaSHCommand {


  @Command
  public Object execute(
      @Usage("The time to wait in seconds")
      @Argument
      String time) throws ScriptException {
    if (time == null) {
      time = 5
    }
    int millis = 1000 * Integer.parseInt(time);
    if (millis < 0) {
      throw new ScriptException("Cannot specify a negative time");
    }
    Thread.sleep(millis);
  }
}