import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.RepositoryService;
import javax.jcr.Credentials;
import javax.jcr.SimpleCredentials;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.Argument;
import org.crsh.shell.Description;

@Description("Connect to a workspace")
class connect extends org.crsh.shell.ClassCommand
{

  @Option(name="-u",aliases=["--username"],usage="user name")
  def String userName;

  @Option(name="-p",aliases=["--password"],usage="password")
  def String password;

  @Option(name="-c",aliases=["--container"],usage="portal container name (eXo portal specific)")
  def String containerName;

  @Argument(required=true,index=0,usage="workspace name")
  def String workspaceName;  

  public Object execute() throws ScriptException {
    def repo;
    def container = ExoContainerContext.getTopContainer();
    if (containerName != null) {
      def portalContainer = container.getPortalContainer(containerName);
      def repoService = portalContainer?.getComponentInstanceOfType(RepositoryService.class);
      repo = repoService?.getRepository('repository');
    } else {
      def repoService = container.getComponentInstanceOfType(RepositoryService.class);
      repo = repoService?.defaultRepository;
    }

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