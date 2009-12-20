import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.RepositoryService;
import javax.jcr.Credentials;
import javax.jcr.SimpleCredentials;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.Argument;

class connect extends org.crsh.shell.ClassCommand
{

  @Option(name="-u",aliases=["--username"],usage="user name")
  def String userName;

  @Option(name="-p",aliases=["--password"],usage="password")
  def String password;

  @Argument(required=true,index=0,usage="workspace name")
  def String workspaceName;  

  public Object execute() throws ScriptException {
  
    def container = ExoContainerContext.getTopContainer();

    if (portalContainerName != null) {
      def portalContainer = container.getPortalContainer(portalContainerName);
      def repoService = portalContainer.getComponentInstanceOfType(RepositoryService.class);
      repo = repoService.getRepository('repository');
    } else {
      def repoService = container.getComponentInstanceOfType(RepositoryService.class);
      repo = repoService.getDefaultRepository();
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