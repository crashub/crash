package crash.commands.test;

import org.crsh.cli.Command;
import org.crsh.command.BaseCommand;

/** @author Julien Viet */
public class java_command extends BaseCommand {

  @Command
  public String main() {
    context.getSession().put("abc", "def");
    return "hello";
  }
}
