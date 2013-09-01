import org.crsh.cli.Usage
import org.crsh.cli.Command
import org.crsh.cli.Argument

class sleep {
  @Usage("sleep for some time")
  @Command
  Object main(@Usage("sleep time in seconds") @Argument int time) {
    if (time < 0)
      throw new ScriptException("Cannot provide negative time value $time");
    Thread.sleep(time * 1000);
    return null;
  }
}