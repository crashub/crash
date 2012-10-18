import org.crsh.cmdline.annotations.Man
import org.crsh.cmdline.annotations.Usage
import org.crsh.cmdline.annotations.Command
import org.crsh.cmdline.annotations.Argument
import org.crsh.cmdline.annotations.Required
import org.crsh.jcr.command.Path
import org.crsh.command.PipeCommand
import javax.jcr.Node
import org.crsh.command.InvocationContext;

@Usage("mixin commands")
@Man("""The mixin command manipulates JCR node mixins. Mixins can be added to or removed from nodes.""")
public class mixin extends org.crsh.jcr.command.JCRCommand {

  // It consumes a node stream or path arguments
  @Usage("add a mixin to one or several nodes")
  @Man("""\
The add command addds a mixin to one or several nodes, this command is a <Node,Void> command, and can
add a mixin from an incoming node stream, for instance:

[/]% select * from mynode | mixin add mix:versionable
""")
  @Command
  public PipeCommand<Node> add(
     InvocationContext<Node> context,
     @Usage("the mixin name to add") @Argument @Required String mixin,
     @Argument @Usage("the paths of the node receiving the mixin") List<Path> paths) {
    assertConnected();
    context.writer << "Mixin $mixin added to nodes";
    return new PipeCommand<Node>() {
      @Override
      void open() {
        perform(paths, this.&provide);
      }
      @Override
      void provide(Node node) {
        node.addMixin(mixin);
        context.provide(node);
        context.writer << " $node.path";
      }
    }
  }

  // It consumes a node stream or path arguments
  @Usage("removes a mixin from one or several nodes")
  @Man("""\
The remove command removes a mixin from one or several nodes, this command is a <Node,Void> command, and can
remove a mixin from an incoming node stream, for instance:

[/]% select * from mynode | mixin remove mix:versionable
""")
  @Command
  public PipeCommand<Node> remove(
      InvocationContext<Node> context,
      @Usage("the mixin name to remove") @Argument @Required String mixin,
      @Argument @Usage("the paths of the node receiving the mixin") List<Path> paths) {
    assertConnected();
    context.writer << "Mixin $mixin removed from nodes";
    return new PipeCommand<Node>() {
      @Override
      void open() {
        perform(paths, this.&provide);
      }
      @Override
      void provide(Node node) {
        node.removeMixin(mixin);
        context.provide(node);
        context.writer << " $node.path";
      }
    }
  }

  private void perform(List<Path> paths, def closure) {
    paths.each { path ->
      def node = getNodeByPath(path);
      closure(node);
    };
  }
}