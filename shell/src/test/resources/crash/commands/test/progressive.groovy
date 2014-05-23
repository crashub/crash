package crash.commands.test;

import org.crsh.cli.Command
import org.crsh.cli.Option
import org.crsh.command.InvocationContext

public class progressive {

  @Command
  public void main(
      InvocationContext<Object> context,
      @Option(names = ["a"]) Integer amount,
      @Option(names = ["s"]) Integer size,
      @Option(names = ["w"]) Integer width) {
    if (amount == null) {
      amount = 10;
    } else if (amount < 0) {
      throw new org.crsh.command.ScriptException("No negative amount $amount allowed");
    }
    if (size == null) {
      size = 10;
    } else if (size < 0) {
      throw new org.crsh.command.ScriptException("No negative size $size allowed");
    }
    if (width == null) {
      width = 32;
    } else if (width < 0) {
      throw new org.crsh.command.ScriptException("No negative width $width allowed");
    }
    def sb = new StringBuilder();
    for (int i = 0;i < size;i++) {
      for (int j = 0;j < width;j++) {
        sb.append((char)(((int)'A') + (i % 26)));
      }
      sb.append('\n');
    }
    def text = sb.toString();
    for (int i = 0; i < amount;i++) {
      context.getWriter() << text;
      context.flush();
      Thread.sleep(1000);
    }
  }
}