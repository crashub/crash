package crash.commands.test

import org.crsh.cmdline.annotations.Argument
import org.crsh.cmdline.annotations.Command
import org.crsh.cmdline.annotations.Usage

public class cls extends org.crsh.command.CRaSHCommand {

  @Command
  @Usage("clear screen")
  public void main() {
    out.cls();
  }
}