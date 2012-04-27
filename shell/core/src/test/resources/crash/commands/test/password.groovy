package crash.commands.test

import org.crsh.cmdline.annotations.Command
import org.crsh.cmdline.annotations.Usage

public class password extends org.crsh.command.CRaSHCommand {

  @Command
  @Usage("prompt a password")
  public void main() {
    out << "the password you typed is : " + readLine("give me your password:", false)
  }
}