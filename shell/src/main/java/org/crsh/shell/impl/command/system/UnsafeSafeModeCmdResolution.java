package org.crsh.shell.impl.command.system;

import org.crsh.cli.Usage;
import org.crsh.command.BaseCommand;
import org.crsh.command.InvocationContext;
import org.crsh.text.Color;
import org.crsh.text.Decoration;
import org.crsh.text.Style;
import org.crsh.text.ui.LabelElement;

import java.util.HashMap;

public class UnsafeSafeModeCmdResolution {
    public static abstract class UnSafeCommand extends BaseCommand {

        UnSafeCommand() {
        }

        public abstract String getCommandName();

        @Usage("Command is not available when shell is in Safe mode.")
        @org.crsh.cli.Command
        public void main(InvocationContext<Object> context) throws Exception {
            context.provide(new LabelElement("Unsafe system/script command [" + getCommandName()
                    + "] is not available with shell in safe mode.").style(Style.style(Decoration.bold, Color.red)));
        }
    }

    static void addSafeHandlers(HashMap<String, Class<? extends BaseCommand>> safeCommands, boolean exitAllowed) {
        if (!exitAllowed) {
            safeCommands.put("bye", Bye.class);
        }
        safeCommands.put("dashboard", Dashboard.class);
        safeCommands.put("egrep", EGrep.class);
        safeCommands.put("env", Env.class);
        if (!exitAllowed) {
            safeCommands.put("exit", Exit.class);
        }
        safeCommands.put("filter", Filter.class);
        safeCommands.put("java", CJava.class);
        safeCommands.put("jdbc", Jdbc.class);
        safeCommands.put("jndi", Jndi.class);
        safeCommands.put("jpa", Jpa.class);
        safeCommands.put("jul", Jul.class);
        safeCommands.put("jvm", Jvm.class);
        safeCommands.put("less", Less.class);
        safeCommands.put("man", CMan.class);
        safeCommands.put("repl", CRepl.class);
        safeCommands.put("shell", CShell.class);
        safeCommands.put("sleep", Sleep.class);
        safeCommands.put("sort", Sort.class);
        safeCommands.put("system", CSystem.class);
        safeCommands.put("thread", CThread.class);
    }
    public static class Bye extends UnSafeCommand { public String getCommandName() { return "bye"; } }
    public static class Dashboard extends UnSafeCommand { public String getCommandName() { return "dashboard"; } }
    public static class EGrep extends UnSafeCommand { public String getCommandName() { return "egrep"; } }
    public static class Env extends UnSafeCommand { public String getCommandName() { return "env"; } }
    public static class Exit extends UnSafeCommand { public String getCommandName() { return "exit"; } }
    public static class Filter extends UnSafeCommand { public String getCommandName() { return "filter"; } }
    public static class CJava extends UnSafeCommand { public String getCommandName() { return "java"; } }
    public static class Jdbc extends UnSafeCommand { public String getCommandName() { return "jdbc"; } }
    public static class Jndi extends UnSafeCommand { public String getCommandName() { return "jndi"; } }
    public static class Jpa extends UnSafeCommand { public String getCommandName() { return "jpa"; } }
    public static class Jul extends UnSafeCommand { public String getCommandName() { return "jul"; } }
    public static class Jvm extends UnSafeCommand { public String getCommandName() { return "jvm"; } }
    public static class Less extends UnSafeCommand { public String getCommandName() { return "less"; } }
    public static class CMan extends UnSafeCommand { public String getCommandName() { return "man"; } }
    public static class CRepl extends UnSafeCommand { public String getCommandName() { return "repl"; } }
    public static class CShell extends UnSafeCommand { public String getCommandName() { return "shell"; } }
    public static class Sleep extends UnSafeCommand { public String getCommandName() { return "sleep"; } }
    public static class Sort extends UnSafeCommand { public String getCommandName() { return "sort"; } }
    public static class CSystem extends UnSafeCommand { public String getCommandName() { return "system"; } }
    public static class CThread extends UnSafeCommand { public String getCommandName() { return "thread"; } }
}
