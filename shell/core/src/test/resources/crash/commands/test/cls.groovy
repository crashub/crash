package crash.commands.test

import org.crsh.cli.Command
import org.crsh.cli.Usage

public class cls extends org.crsh.command.CRaSHCommand {

  @Command
  @Usage("clear screen")
  public void main() {
    out.cls();
  }
}