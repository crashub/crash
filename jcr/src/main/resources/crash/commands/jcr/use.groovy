import org.crsh.cmdline.annotations.Argument
import org.crsh.cmdline.annotations.Command
import org.crsh.cmdline.annotations.Man
import org.crsh.cmdline.annotations.Usage
import org.crsh.jcr.JCR
import org.crsh.jcr.command.InitProperties

class use extends org.crsh.jcr.command.JCRCommand {
  @Usage("changes the current repository")
  @Man("""
  The use command changes the current repository used by for JCR commands. The command must at least
  have a URL parameter to be used in connecting to the repository.

  % use org.apache.jackrabbit.repository.uri=rmi://localhost:1099/jackrabbit
  % use parameterName=parameterValue;nextParameterName=nextParameterValue
""")
  @Command
  public Object main(
      @Argument
      @Usage("the parameters")
      @Man("The parameters used to instantiate the repository to be used in this session") InitProperties parameters) throws ScriptException {
       repo = JCR.getRepository(parameters.getProperties());
    }
}
