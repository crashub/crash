import org.crsh.cli.Usage
import org.crsh.cli.Argument
import org.crsh.cli.Command
import org.crsh.cli.Option;

public class echo {

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