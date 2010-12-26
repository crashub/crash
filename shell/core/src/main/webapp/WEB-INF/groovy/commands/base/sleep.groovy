import org.crsh.command.Description;
import org.crsh.cmdline.Command;
import org.crsh.cmdline.Argument;
import org.crsh.command.ScriptException;


class sleep extends org.crsh.command.CRaSHCommand
{

  @Command(description = "Provides basic help")
  Object main(@Argument(description = "Sleep time in seconds") int time) throws ScriptException {
    if (time < 0)
      throw new ScriptException("Cannot provide negative time value $time");
    Thread.sleep(time * 1000);
    return null;
  }
}