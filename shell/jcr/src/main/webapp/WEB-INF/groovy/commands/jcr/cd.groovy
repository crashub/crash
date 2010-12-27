import org.crsh.jcr.command.PathArg;

public class commit extends org.crsh.jcr.command.JCRCommand {

  @Command(description="Change the current directory")
  public Object main(@PathArg String path) throws ScriptException {
    assertConnected();
    def node = findNodeByPath(path);
    setCurrentNode(node);
  }
}

