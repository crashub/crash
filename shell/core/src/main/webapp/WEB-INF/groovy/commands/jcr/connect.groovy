import javax.jcr.Credentials;
import javax.jcr.SimpleCredentials;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.Argument;
import org.crsh.command.Description;
import org.crsh.jcr.JCR;

@Description("Connect to a workspace")
class connect extends org.crsh.command.ClassCommand
{

  @Option(required=true,name="-u",aliases=["--username"],usage="user name")
  def String userName;

  @Option(name="-p",aliases=["--password"],usage="password")
  def String password;

  @Option(name="-c",aliases=["--container"],usage="portal container name (eXo portal specific)")
  def String containerName;

  @Argument(required=true,index=0,usage="workspace name")
  def String workspaceName;  

  public Object execute() throws ScriptException {

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
}