import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.RepositoryService;
import javax.jcr.Credentials;
import javax.jcr.SimpleCredentials;
{ name, Object[] args ->
  def container = ExoContainerContext.getTopContainer();

  //
  def repo;
  if (portalContainerName != null) {
    def portalContainer = container.getPortalContainer(portalContainerName);
    def repoService = portalContainer.getComponentInstanceOfType(RepositoryService.class);
    repo = repoService.getRepository('repository');
  } else {
    def repoService = container.getComponentInstanceOfType(RepositoryService.class);
    repo = repoService.getDefaultRepository();
  }

  //
  if (args.length == 2 && args[0] != null && args[1] != null) {
    def credentials = new SimpleCredentials(args[0], args[1].toCharArray());
    session = repo.login(credentials, name);
  } else {
    session = repo.login(name);
  }
  def root = session.getRootNode();
  setCurrentNode(root);
  return """Connected to workspace $name""";
}