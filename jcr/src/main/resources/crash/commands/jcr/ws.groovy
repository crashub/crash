import javax.jcr.Credentials;
import javax.jcr.SimpleCredentials;
import org.crsh.jcr.JCR;

@Man("""\
The ws command provides a set of commands interacting with JCR workspace.""")
class ws extends org.crsh.jcr.command.JCRCommand
{

  @Usage("login to a workspace")
  @Man("""
This command login to a JCR workspace and establish a session with the repository.
When you are connected the shell maintain a JCR session and allows you to interact with the session in a shell
oriented fashion. The repository name must be specified and optionally you can specify a user name and password to
have more privileges.

% connect -c portal portal-system
Connected to workspace portal-system

% connect -c portal -u root -p gtn portal-system
Connected to workspace portal-system

""")
  @Command
  public Object login(
    @Option(names=["u","username"])
    @Usage("the user name")
    String userName,
    @Option(names=["p","password"])
    @Usage("the user passowrd")
    def String password,
    @Option(names=["c","container"])
    @Usage("portal container name (eXo JCR specific)")
    def String containerName,
    @Argument
    @Required
    @Usage("the workspace name")
    def String workspaceName) throws ScriptException {

    //
    if (userName != null && password == null) {
      password = readLine("password:", false);
    }

    //
    def properties = containerName == null ? [:] : ["exo.container.name":containerName];

    //
    def repo = JCR.getRepository(properties);

    //
    if (repo == null) {
      return "Could not locate repository";
    }

    //
    if (userName != null && password != null) {
      def credentials = new SimpleCredentials(userName, password.toCharArray());
      session = repo.login(credentials, workspaceName);
    } else {
      session = repo.login(workspaceName);
    }
    def root = session.getRootNode();
    setCurrentNode(root);
    return """Connected to workspace $workspaceName""";
  }

  @Usage("logout from a workspace")
  @Man("""This command logout from the currently connected JCR workspace""")
  @Command
  public Object logout() throws ScriptException {
    assertConnected();
    session.logout();
    session = null;
    currentPath = null;
    return "Disconnected from workspace";
  }
}