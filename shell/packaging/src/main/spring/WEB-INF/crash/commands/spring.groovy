package crash.commands.base

import org.crsh.command.CRaSHCommand
import org.crsh.cmdline.annotations.Usage
import org.crsh.cmdline.annotations.Command
import org.crsh.cmdline.annotations.Argument

import org.crsh.cmdline.annotations.Required
import org.crsh.text.ui.UIBuilder

@Usage("Spring commands")
class spring extends CRaSHCommand {

  @Usage("list the beans")
  @Command
  public void ls() {
    UIBuilder ui = new UIBuilder()
    ui.table() {
      row(decoration: bold, foreground: black, background: white) {
        label("BEAN"); label("TYPE"); label("VALUE")
      }
      context.attributes.beans.each { key, value ->
        row() {
          label(value: key, foreground: red); label(value?.getClass().name); label("" + value)
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
