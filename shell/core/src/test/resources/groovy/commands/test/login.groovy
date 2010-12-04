import javax.jcr.Credentials;
import javax.jcr.SimpleCredentials;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.Argument;
import org.crsh.command.Description;
import org.crsh.jcr.JCR;

@Description("Login to a workspace")
class login extends org.crsh.command.ClassCommand
{

  @Argument(required=true,index=0,usage="workspace name")
  def String workspaceName;

  public Object execute() throws ScriptException {

    //
    def properties = containerName == null ? [:] : ["exo.container.name":containerName];

    //
    def repo = JCR.getRepository(properties);

    //
    if (repo == null) {
      return "Could not locate repository";
    }

    //
    session = repo.login(workspaceName);

    //
    def root = session.getRootNode();
    setCurrentNode(root);
    return """Connected to workspace $workspaceName""";
  }
}