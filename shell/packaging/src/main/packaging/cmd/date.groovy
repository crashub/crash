import org.crsh.command.CRaSHCommand
import org.crsh.cmdline.annotations.Usage
import org.crsh.cmdline.annotations.Command
import org.crsh.cmdline.annotations.Option

class date extends CRaSHCommand {
  @Usage("show the current time")
  @Command
  Object main(@Usage("the time format") @Option(names=["f","format"]) String format) throws ScriptException {
    if (format == null)
      format = "EEE MMM d HH:mm:ss z yyyy";
    def date = new Date();
    return date.format(format);
  }
}