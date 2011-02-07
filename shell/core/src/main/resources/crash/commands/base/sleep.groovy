import org.crsh.command.CRaSHCommand
import org.crsh.cmdline.annotations.Usage
import org.crsh.cmdline.annotations.Command
import org.crsh.cmdline.annotations.Argument

class sleep extends CRaSHCommand {
  @Usage("sleep for some time")
  @Command
  Object main(@Usage("Sleep time in seconds") @Argument int time) throws ScriptException {
    if (time < 0)
      throw new ScriptException("Cannot provide negative time value $time");
    Thread.sleep(time * 1000);
    return null;
  }
}