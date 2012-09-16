import org.crsh.command.CRaSHCommand
import org.crsh.cmdline.annotations.Usage
import org.crsh.cmdline.annotations.Command
import org.crsh.cmdline.annotations.Option

class top extends CRaSHCommand {
  @Usage("display and update sorted information about processes")
  @Command
  void main(
      @Usage("Set the delay between updates to <delay> seconds. The default delay between updates is 1 second.")
      @Option(names = 's') Integer delay) {
    if (delay == null)
      delay = 1
    if (delay < 0)
      throw new ScriptException("Cannot provide negative time value $delay");
    while (!Thread.interrupted()) {
      out.cls()
      thread.ls();
      out.flush();
      Thread.sleep(delay * 1000);
    }
  }
}
