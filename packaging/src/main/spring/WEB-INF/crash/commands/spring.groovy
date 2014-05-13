package crash.commands.base

import org.crsh.cli.Usage
import org.crsh.cli.Command
import org.crsh.cli.Argument

import org.crsh.cli.Required
import org.crsh.text.ui.Overflow
import org.crsh.text.ui.UIBuilder

@Usage("Spring commands")
class spring {

  @Usage("list the beans")
  @Command
  public void ls() {
    def ui = new UIBuilder().table(overflow: Overflow.HIDDEN, rightCellPadding: 1) {
      row(decoration: bold, foreground: black, background: white) {
        label("BEAN"); label("TYPE"); label("VALUE")
      }
      context.attributes.beans.each { key, value ->
        row() {
          label(value: key, foreground: red); label(value?.getClass()?.name); label("" + value)
        }
      }
    }
    context.writer.print(ui);
  }

  @Usage("determines if the specified bean is a singleton or not")
  @Command
  public String singleton(@Usage("the bean name") @Argument(name = 'bean name') @Required String name) {
    return "Bean $name is ${context.attributes.factory.isSingleton(name) ? '' : 'not '}a singleton";
  }
}
