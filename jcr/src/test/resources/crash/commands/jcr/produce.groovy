import org.crsh.command.ScriptException;
import org.crsh.command.InvocationContext
import org.crsh.cmdline.annotations.Command
import org.crsh.cmdline.annotations.Usage
import org.crsh.jcr.command.Path
import org.crsh.cmdline.annotations.Argument;

public class produce extends org.crsh.command.CRaSHCommand  {

  @Command
  @Usage("produce a set of nodes")
  public void main(
    InvocationContext<Void, Node> context,
    @Path @Argument @Usage("the paths") List<String> paths) throws ScriptException {
    assertConnected();
    paths.each {
      try {
        def node = getNodeByPath(it)
        System.out.println("produced " + node);
        context.produce(node);
      }
      catch (Throwable t) {
        t.printStackTrace();
      }
    }
  }
}