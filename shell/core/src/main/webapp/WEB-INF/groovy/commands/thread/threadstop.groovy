import org.crsh.cmdline.Argument;
import org.crsh.cmdline.Option;
import org.crsh.command.ScriptException;
import org.crsh.command.Description;
import org.crsh.command.CommandContext;
import java.util.Collections;
import java.util.regex.Pattern;

@Description("Stop threads, use with care!")
public class threadstop extends org.crsh.command.CRaSHCommand<Thread, Void> {

  public void execute(CommandContext<Thread, Void> context) throws ScriptException {
    if (context.piped) {
      context.consume().each() {
        it.stop();
        context.writer.println("Kill thread $it");
      }
    }
  }
}

