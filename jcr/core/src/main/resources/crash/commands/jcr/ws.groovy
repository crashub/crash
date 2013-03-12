import javax.jcr.SimpleCredentials;

import org.crsh.jcr.command.ContainerOpt;
import org.crsh.jcr.command.UserNameOpt;
import org.crsh.jcr.command.PasswordOpt
import org.crsh.cli.Man
import org.crsh.cli.Usage
import org.crsh.cli.Command
import org.crsh.cli.Argument
import org.crsh.cli.Required;

@Usage("workspace commands")
@Man("""The ws command provides a set of commands interacting with JCR workspace.""")
class ws extends org.crsh.jcr.command.JCRCommand
{

  @Usage("login to a workspace")
  @Man("""
This command login to a JCR workspace and establish a session with the repository.
When you are connected the shell maintain a JCR session and allows you to interact with the session in a shell
oriented fashion. The repository name must be specified and optionally you can specify a user name and password to
have more privileges.

Before performing a login operation, a repository must be first selected with the repo command, for instance:

% repo use container=portal

Once a repository is obtained the login operation can be done:

% ws login portal-system
Connected to workspace portal-system

% ws login -u root -p gtn portal-system
Connected to workspace portal-system

""")
  @Command
  public Object login(
    @UserNameOpt String userName,
    @PasswordOpt String password,
    @ContainerOpt String containerName,
    @Argument
    @Required
    @Usage("the workspace name")
    @Man("The name of the workspace to connect to")
    String workspaceName) {

    //
    if (userName != null && password == null) {
      password = readLine("password:", false);
    }

    //
    if (containerName != null) {
      throw new ScriptException("The container name option is legacy, use 'repo use container=" + containerName + "' instead")
    }

    //
    if (repository == null) {
      throw new ScriptException("No repository selected, use the repo command first");
    }

    //
    if (userName != null && password != null) {
      def credentials = new SimpleCredentials(userName, password.toCharArray());
      session = repository.login(credentials, workspaceName);
    } else {
      session = repository.login(workspaceName);
    }
    def root = session.getRootNode();
    setCurrentNode(root);
    return """Connected to workspace $workspaceName""";
  }

  @Usage("logout from a workspace")
  @Man("""This command logout from the currently connected JCR workspace""")
  @Command
  public Object logout() {
    assertConnected();
    session.logout();
    session = null;
    currentPath = null;
    return "Disconnected from workspace";
  }
}