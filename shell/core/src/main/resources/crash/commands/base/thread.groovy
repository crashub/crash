import java.util.regex.Pattern;
import org.crsh.command.CRaSHCommand
import org.crsh.cmdline.annotations.Usage
import org.crsh.cmdline.annotations.Command
import org.crsh.command.InvocationContext
import org.crsh.cmdline.annotations.Option
import org.crsh.cmdline.annotations.Man;

@Usage("vm thread commands")
public class thread extends CRaSHCommand {

  @Usage("list the vm threads")
  @Command
  public void ls(
    InvocationContext<Void, Thread> context,
    @Usage("Retain the thread with the specified name")
    @Option(names=["n","name"])
    String name,
    @Usage("Filter the threads with a regular expression on their name")
    @Option(names=["f","filter"])
    String nameFilter,
    @Usage("Filter the threads by their status (new,runnable,blocked,waiting,timed_waiting,terminated)")
    @Option(names=["s","state"])
    String stateFilter) throws ScriptException {

    // Regex filter
    if (name != null) {
      nameFilter = Pattern.quote(name);
    } else if (nameFilter == null) {
      nameFilter = ".*";
    }
    def pattern = Pattern.compile(nameFilter);

    // State filter
    Thread.State state = null;
    if (stateFilter != null) {
      try {
        state = Thread.State.valueOf(stateFilter.toUpperCase());
      } catch (IllegalArgumentException iae) {
        throw new ScriptException("Invalid state filter $stateFilter");
      }
    }

    //
    ThreadGroup root = getRoot();
    Thread[] threads = new Thread[root.activeCount()];
    while (root.enumerate(threads, true) == threads.length ) {
      threads = new Thread[threads.length * 2];
    }

    //    
    def formatString = "%1\$-3s %2\$-8s %3\$-13s %4\$-20s\r\n";
    Formatter formatter = new Formatter(context.writer);
    formatter.format(formatString, "ID", "PRIORITY", "STATE", "NAME");

    //
    threads.each() {
      if (it != null) {
        def matcher = it.name =~ pattern;
        if (matcher.matches() && (state == null || it.state == state)) {
          formatter.format(formatString, it.id, it.priority, "$it.state", it.name);
          context.produce(it);
        }
      }
    }
  }
  
  private ThreadGroup getRoot() {
    ThreadGroup group = Thread.currentThread().threadGroup;
    ThreadGroup parent;
    while ((parent = group.parent) != null) {
      group = parent;
    }
    return group;
  }

  @Usage("stop vm threads")
  @Man("Stop a VM thread, this method cannot be called as is and should be used with a pipe to consume a list of threads.")
  @Command
  public void stop(InvocationContext<Thread, Void> context) throws ScriptException {
    if (context.piped) {
      context.consume().each() {
        it.stop();
        context.writer.println("Kill thread $it");
      }
    }
  }
}

