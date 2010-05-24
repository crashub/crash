import org.crsh.command.Description;
import org.kohsuke.args4j.Argument;
import org.crsh.command.Description;
import org.crsh.command.ScriptException;
import java.lang.reflect.Method;

@Description("Invoke a static method")
public class invoke extends org.crsh.command.ClassCommand {

  @Argument(required=false,index=0,usage="The fqn of the class to resolve")
  def String className;

  @Argument(required=false,index=1,usage="The method to invoke")
  def String methodName;

  public Object execute() throws ScriptException {
    Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
    Method m = clazz.getMethod(methodName);
    m.invoke(null);
  }
}