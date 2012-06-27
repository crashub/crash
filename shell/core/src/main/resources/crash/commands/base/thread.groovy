import java.util.regex.Pattern;
import org.crsh.command.CRaSHCommand
import org.crsh.cmdline.annotations.Usage
import org.crsh.cmdline.annotations.Command
import org.crsh.command.InvocationContext
import org.crsh.cmdline.annotations.Option
import org.crsh.cmdline.annotations.Man
import org.crsh.shell.ui.UIBuilder
import org.crsh.cmdline.annotations.Argument

@Usage("JVM thread commands")
@Man("""\
The thread command provides introspection and control over JVM threads:

% thread ls
ID   PRIORITY  STATE          INTERRUPTED  DAEMON  NAME
2    10        WAITING        false        true    Reference Handler
3    8         WAITING        false        true    Finalizer
6    9         RUNNABLE       false        true    Signal Dispatcher
1    5         WAITING        false        false   main
13   1         TIMED_WAITING  false        true    Poller SunPKCS11-Darwin
14   5         WAITING        false        false   pool-1-thread-1
15   5         WAITING        false        false   pool-1-thread-2
16   5         WAITING        false        false   pool-1-thread-3
17   5         WAITING        false        false   pool-1-thread-4
27   5         WAITING        false        false   pool-1-thread-6
19   5         RUNNABLE       false        false   org.crsh.standalone.CRaSH.main()

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

    ui.table() {
      row(decoration: bold, foreground: black, background: white) {
        label("ID"); label("PRIORITY"); label("STATE"); label("INTERRUPTED"); label("DAEMON"); label("NAME")
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
            row(background: c, foreground: black) {
              label(thread.id); label(thread.priority); label(thread.state); label(thread.isInterrupted()); label(thread.daemon); label(thread.name)
            }
            context.produce(it);
          }
        }
      }
    }

    context.writer.print(ui);

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

