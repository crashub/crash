import org.crsh.command.Description;
import org.kohsuke.args4j.Argument;
import org.crsh.command.Description;
import org.crsh.command.ScriptException;
import java.lang.reflect.Method;

@Description("Invoke a static method")
public class wait extends org.crsh.command.ClassCommand {

  @Argument(required=false,index=0,usage="The time to wait in seconds")
  def String time;

  public Object execute() throws ScriptException {
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