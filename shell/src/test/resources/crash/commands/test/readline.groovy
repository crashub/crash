package crash.commands.test

import org.crsh.cli.Command
import org.crsh.cli.Option
import org.crsh.cli.Usage

public class readline {

  @Command
  @Usage("read a line of text and print it")
  public String main(@Option(names = ["hidden"]) Boolean hidden) {
    try {
      String s = context.readLine("Give me something:", !hidden);
      return "You gave $s";
    } catch (InterruptedException e) {
      return "was interrupted when reading value";
    }
  }
}