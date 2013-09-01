import org.crsh.cli.Usage
import org.crsh.cli.Command

import org.crsh.cli.Option
import org.crsh.cli.Argument

public class fail {

  @Usage("fails in an configurable manner")
  @Command
  Object main(
    @Option(names=["t","type"]) @Usage("the error kind") Type type,
    @Argument @Usage("the error message") String msg) {

    switch (type ?: Type.CHECKED) {
      case Type.CHECKED:
        if (msg != null) {
          throw new Exception(msg);
        } else {
          throw new Exception();
        }
      case Type.RUNTIME:
        if (msg != null) {
          throw new RuntimeException(msg);
        } else {
          throw new RuntimeException();
        }
      case Type.SCRIPT:
        if (msg != null) {
          throw new org.crsh.command.ScriptException(msg);
        } else {
          throw new org.crsh.command.ScriptException();
        }
      case Type.ERROR:
        if (msg != null) {
          throw new Error(msg);
        } else {
          throw new Error();
        }
      default:
        throw new AssertionError();
    }
  }

  enum Type {

    CHECKED,

    RUNTIME,

    SCRIPT,

    ERROR

  }

}