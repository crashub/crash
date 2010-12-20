import org.crsh.cmdline.Argument;
import org.crsh.cmdline.Option;
import org.crsh.command.ScriptException;
import org.crsh.command.Description;
import org.crsh.command.CommandContext;
import java.util.Collections;
import java.util.regex.Pattern;
import java.util.Formatter;

@Description("List the vm threads")
public class threadls extends org.crsh.command.BaseCommand<Void, Thread> {

  @Description("Retain the thread with the specified name")
  // @Option(name="-n",aliases=["--name"],usage="Retain the thread with the specified name", required=false)
  @Option(names="n")
  def String name;

  @Description("Filter the threads with a regular expression on their name")
  // @Option(name="-f",aliases=["--filter"],usage="Filter the threads with a regular expression on their name", required=false)
  @Option(names="-f")
  def String nameFilter;

  @Description("Filter the threads by their status (new,runnable,blocked,waiting,timed_waiting,terminated)")
  // @Option(name="-s",aliases=["--state"],usage="Filter the threads by their status (new,runnable,blocked,waiting,timed_waiting,terminated)", required=false)
  @Option(names="s")
  def String stateFilter;

  public void execute(CommandContext<Void, Thread> context) throws ScriptException {

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
}

