package crash.commands.test

import org.crsh.cli.Command
import org.crsh.cli.Usage

public class password {

  @Command
  @Usage("prompt a password")
  public void main() {
    out << "the password you typed is : " + readLine("give me your password:", false)
  }
}