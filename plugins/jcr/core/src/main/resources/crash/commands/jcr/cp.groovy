import org.crsh.cli.Man
import org.crsh.cli.Command
import org.crsh.cli.Usage
import org.crsh.cli.Required
import org.crsh.cli.Argument
import org.crsh.jcr.command.Path;

public class cp extends org.crsh.jcr.command.JCRCommand {

  @Usage("copy a node to another")
  @Command
  @Man("""\
The cp command copies a node to a target location in the JCR tree.

[/registry]% cp foo bar""")
  public void main(
    @Required @Usage("the source path") @Man("The path of the source node to copy") @Argument Path source,
    @Required @Usage("the target path") @Man("The path of the target node to be copied") @Argument Path target) {
    assertConnected();

    //
    def sourceNode = findItemByPath(source);

    //
    def targetPath = absolutePath(target);

    //
    sourceNode.session.workspace.copy(sourceNode.path, targetPath.value);
  }
}