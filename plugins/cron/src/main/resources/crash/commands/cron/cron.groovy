package crash.commands.cron

import org.crsh.cli.Argument
import org.crsh.cli.Command
import org.crsh.cli.Usage
import org.crsh.command.InvocationContext
import org.crsh.cron.CRaSHTaskProcess
import org.crsh.cron.CronPlugin
import org.crsh.groovy.GroovyCommand
import org.crsh.plugin.PluginContext
import org.crsh.shell.impl.command.CRaSH;

@Usage("manages the cron plugin")
public class cron extends GroovyCommand {

  private CronPlugin getCronPlugin() {
    PluginContext context = getPluginContext()
    return context.getPlugin(CronPlugin.class);
  }

  private PluginContext getPluginContext() {
    CRaSH crash = (CRaSH)context.session["crash"];
    def context = crash.getContext()
    context
  }

  @Command
  @Usage("display the process history")
  public void history(InvocationContext<Map> context) {
    CronPlugin plugin = getCronPlugin();
    for (CRaSHTaskProcess process : plugin.history) {
      context.provide([
          ACTIVE: process.active,
          PATTERN: process.schedulingPattern,
          STARTED: new Date(process.time),
          LINE: process.line
      ]);
    }
  }

  @Command
  @Usage("trigger the cron service now")
  public void spawn() {
    CronPlugin plugin = getCronPlugin();
    plugin.spawn();
  }

  @Command
  @Usage("read or write the cron config path")
  public void config(
      @Usage("the new config path when specified")
      @Argument
      String path) {
    def pluginContext = getPluginContext()
    if (path != null) {
      pluginContext.setProperty(CronPlugin.CRON_CONFIG_PATH, path);
    }
    String value = pluginContext.getProperty(CronPlugin.CRON_CONFIG_PATH);
    out << "Current config path ${value}";
  }

}