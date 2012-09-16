import java.util.regex.Pattern;
import org.crsh.command.CRaSHCommand
import org.crsh.cmdline.annotations.Usage
import org.crsh.cmdline.annotations.Command
import org.crsh.command.InvocationContext
import org.crsh.cmdline.annotations.Option
import org.crsh.cmdline.annotations.Man
import org.crsh.text.ui.UIBuilder
import org.crsh.cmdline.annotations.Argument
import java.lang.management.ThreadMXBean
import sun.management.ManagementFactory

@Usage("JVM thread commands")
@Man("""\
The thread command provides introspection and control over JVM threads:

% thread ls
ID     PRIORITY     STATE        %CPU     TIME     INTERRUPTED     DAEMON     GROUP    NAME
2      10           WAITING      0        0:0      false           true       system   Reference Handler
3      8            WAITING      0        0:0      false           true       system   Finalizer
6      9            RUNNABLE     0        0:0      false           true       system   Signal Dispatcher
1      5            WAITING      0        0:2      false           false      main     main
13     5            WAITING      0        0:0      false           false      main     pool-1-thread-1

% thread stop 14
Stopped thread Thread[pool-1-thread-1,5,main]

% thread interrupt 17
Interrupted thread Thread[pool-1-thread-1,5,main]

In addition of the classical usage, the various commands (ls, stop, interrupt) can be combined
with a pipe, the most common operation is to combine the ls command with the stop, interrupt or dump command,
for instance the following command will interrupt all the thread having a name starting with the 'pool' prefix:

% thread ls --filter pool.* | thread interrupt
Interrupted thread Thread[pool-1-thread-1,5,main]
Interrupted thread Thread[pool-1-thread-2,5,main]
Interrupted thread Thread[pool-1-thread-3,5,main]
Interrupted thread Thread[pool-1-thread-4,5,main]
Interrupted thread Thread[pool-1-thread-5,5,main]""")
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
    String stateFilter) {

    // Regex filter
    if (name != null) {
      nameFilter = Pattern.quote(name);
    } else if (nameFilter == null) {
      nameFilter = ".*";
    }
    def pattern = Pattern.compile(nameFilter);

    //
    ThreadMXBean threadMXBean = ManagementFactory.threadMXBean;

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
    Map<String, Thread> threads = getThreads();

    //
    UIBuilder ui = new UIBuilder();

    // Sample CPU
    Map<Long, Long> times1 = new HashMap<Long, Long>(threads.size())
    threads.values().each { thread ->
      long cpu = threadMXBean.getThreadCpuTime(thread.id);
      times1[thread.id] = cpu;
    }

    // Sleep 100ms
    Thread.sleep(100);

    // Resample
    Map<Long, Long> times2 = new HashMap<Long, Long>(threads.size())
    threads.values().each { thread ->
      long cpu = threadMXBean.getThreadCpuTime(thread.id);
      times2[thread.id] = cpu;
    }

    // Compute delta map and total time
    long total = 0;
    Map<Long, Long> deltas = new HashMap<Long, Long>(threads.size())
    times2.keySet().each { id ->
      long time1 = times2[id];
      long time2 = times1[id];
      if (time1 == -1) {
        time1 = time2;
      } else if (time2 == -1) {
        time2 = time1;
      }
      def delta = time2 - time1
      deltas[id] = delta;
      total += delta;
    }

    //
    ui.table(weights:[1,1,1,1,1,1,1,2,5]) {
      row(decoration: bold, foreground: black, background: white) {
        label("ID")
        label("PRIORITY")
        label("STATE")
        label("%CPU")
        label("TIME")
        label("INTERRUPTED")
        label("DAEMON")
        label("GROUP")
        label("NAME")
      }
      threads.each() {
        if (it != null) {
          def matcher = it.value.name =~ pattern;
          def thread = it.value;
          if (matcher.matches() && (state == null || it.value.state == state)) {
            switch (it.value.state) {
              case Thread.State.NEW:
                c = cyan
                break;
              case Thread.State.RUNNABLE:
                c = green
                break;
              case Thread.State.BLOCKED:
                c = red
                break;
              case Thread.State.WAITING:
                c = yellow
                break;
              case Thread.State.TIMED_WAITING:
                c = magenta
                break;
              case Thread.State.TERMINATED:
                c = blue
                break;
            }

            // Compute time
            int seconds = times2[it.value.id] / 1000000000;
            int min = seconds / 60
            def time = "${min}:${seconds % 60}";
            def cpu = Math.round((deltas[it.value.id] * 100) / total);

            //
            def group = thread.threadGroup?.name?:"";

            //
            row(background: c, foreground: black) {
              label(thread.id);
              label(thread.priority);
              label(thread.state);
              label(cpu);
              label(time);
              label(thread.isInterrupted());
              label(thread.daemon);
              label(group);
              label(thread.name)
            }

            //
            context.produce(it.getValue());
          }
        }
      }
    }

    //
    out.print(ui);
  }
    
  @Usage("interrupt vm threads")
  @Man("Interrup VM threads.")
  @Command
  public void interrupt(
    InvocationContext<Thread, Void> context,
    @Argument @Usage("the thread ids to interrupt") List<String> ids) {
    apply(context, ids, {
      it.interrupt();
      context.writer.println("Interrupted thread $it");
    })
  }

  @Usage("stop vm threads")
  @Man("Stop VM threads.")
  @Command
  public void stop(
    InvocationContext<Thread, Void> context,
    @Argument @Usage("the thread ids to stop") List<String> ids) {
    apply(context, ids, {
      it.stop();
      context.writer.println("Stopped thread $it");
    })
  }

  @Usage("dump vm threads")
  @Man("Dump VM threads.")
  @Command
  public void dump(
          InvocationContext<Thread, Void> context,
          @Argument @Usage("the thread ids to dump") List<String> ids) {
    apply(context, ids, {
      Exception e = new Exception("Thread ${it.id} stack trace")
      e.setStackTrace(it.stackTrace)
      e.printStackTrace(context.writer)
    })
  }

  public void apply(InvocationContext<Thread, Void> context, List<String> ids, Closure closure) {
    if (context.piped) {
      context.consume().each(closure)
    } else {
      Map<String, Thread> threadMap = getThreads();
      List<String> threads = [];
      for (String id : ids) {
        Thread thread = threadMap[id];
        if (thread != null) {
          threads << thread;
        } else {
          throw new ScriptException("Thread $id does not exist");
        }
      }
      threads.each(closure);
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

  private Map<String, Thread> getThreads() {
    ThreadGroup root = getRoot();
    Thread[] threads = new Thread[root.activeCount()];
    while (root.enumerate(threads, true) == threads.length ) {
      threads = new Thread[threads.length * 2];
    }
    def map = [:];
    threads.each { thread ->
      if (thread != null)
        map["${thread.id}"] = thread
    }
    return map;
  }
}

