import java.lang.reflect.Method
import org.crsh.cli.Usage
import org.crsh.cli.Argument
import org.crsh.cli.Command;

public class invoke {

  @Usage("invoke a static method")
  @Command
  public Object main(
      @Usage("The fqn of the class to resolve")
      @Argument
      String className,
      @Usage("The method to invoke")
      @Argument
      String methodName) {
    Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
    Method m = clazz.getMethod(methodName);
    m.invoke(null);
  }
}