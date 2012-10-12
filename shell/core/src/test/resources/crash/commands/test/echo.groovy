import org.crsh.cmdline.annotations.Usage
import org.crsh.cmdline.annotations.Argument
import org.crsh.cmdline.annotations.Command
import org.crsh.cmdline.annotations.Option;

public class echo extends org.crsh.command.CRaSHCommand {

  @Command
  @Usage("echo text")
  public void main(
      @Usage("flush the content") @Option(names = ["f","flush"]) Integer flush,
      @Usage("the content") @Argument List<String> arguments) {
    for (int i = 0;i < arguments.size();i++) {
      out << arguments[i];
      if (i < flush) {
        out.flush();
      }
    }
  }
}