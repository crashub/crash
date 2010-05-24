import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.RepositoryService;
import javax.jcr.Credentials;
import javax.jcr.SimpleCredentials;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.Argument;
import org.crsh.command.Description;

@Description("Provides basic help")
class help extends org.crsh.command.ClassCommand
{

  /** . */
  private static final List<String> commands = [
  "addmixin",
  "cd",
  "checkin",
  "checkout",
  "commit",
  "connect",
  "disconnect",
  "exportnode",
  "help",
  "importnode",
  "ls",
  "pwd",
  "rm",
  "rollback",
  "select",
  "set"];

  public Object execute() throws ScriptException {

    return """Try one of these commands with the -h or --help switch $commands""";
  }
}