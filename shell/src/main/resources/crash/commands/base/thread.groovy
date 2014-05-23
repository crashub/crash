package crash.commands.base

import java.util.regex.Pattern;
import org.crsh.cli.Usage
import org.crsh.cli.Command
import org.crsh.command.InvocationContext
import org.crsh.cli.Option
import org.crsh.cli.Man
import org.crsh.cli.Argument

import org.crsh.command.Pipe
import org.crsh.text.ui.UIBuilder
import org.crsh.util.Utils

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

In addition of the classical usage, the various commands (ls, stop, interrupt) can be
combined with a pipe, the most common operation is to combine the ls command with the stop,
interrupt or dump command, for instance the following command will interrupt all the thread
having a name starting with the 'pool' prefix:

% thread ls --filter pool.* | thread interrupt
Interrupted thread Thread[pool-1-thread-1,5,main]
Interrupted thread Thread[pool-1-thread-2,5,main]
Interrupted thread Thread[pool-1-thread-3,5,main]
Interrupted thread Thread[pool-1-thread-4,5,main]
Interrupted thread Thread[pool-1-thread-5,5,main]""")
public class thread  {

  /** . */
  private static final Pattern ANY = Pattern.compile(".*");

  @Usage("thread top")
  @Command
  public void top(
    @Usage("Filter the threads with a glob expression on their name")
    @Option(names=["n","name"])
    String nameFilter,
    @Usage("Filter the threads with a glob expression on their group")
    @Option(names=["g","group"])
    String groupFilter,
    @Usage("Filter the threads by their status (new,runnable,blocked,waiting,timed_waiting,terminated)")
    @Option(names=["s","state"])
    String stateFilter) {
    def table = new UIBuilder().table(columns:[1]) {
      header(bold: true, fg: black, bg: white) {
        label("top");
      }
      row {
        eval {
          def args = [:];
          if (nameFilter != null) {
            args.name = nameFilter
          }
          if (stateFilter != null) {
            args.state = stateFilter;
          }
          if (groupFilter != null) {
            args.group = groupFilter;
          }
          // We need to use getProperty otherwise "thread" resolve to this class as a java.lang.Class object
          getProperty("thread").ls args;
        }
      }
    }
    context.takeAlternateBuffer();
    try {
      while (!Thread.currentThread().isInterrupted()) {
        out.cls()
        out.show(table);
        out.flush();
        try {
          Thread.sleep(1000);
        }
        catch (InterruptedException e) {
          Thread.currentThread().interrupt()
        }
      }
    }
    finally {
      context.releaseAlternateBuffer();
    }
  }

  @Usage("list the vm threads")
  @Command
  public void ls(
    InvocationContext<Thread> context,
    @Usage("Filter the threads with a glob expression on their name")
    @Option(names=["n","name"])
    String nameFilter,
    @Usage("Filter the threads with a glob expression on their group")
    @Option(names=["g","group"])
    String groupFilter,
    @Usage("Filter the threads by their status (new,runnable,blocked,waiting,timed_waiting,terminated)")
    @Option(names=["s","state"])
    String stateFilter) {

    // Group filter
    Pattern groupPattern;
    if (groupFilter != null) {
      groupPattern = Pattern.compile('^' + Utils.globexToRegex(groupFilter) + '$');
    } else {
      groupPattern = ANY;
    }

    // Name filter
    Pattern namePattern;
    if (nameFilter != null) {
      namePattern = Pattern.compile('^' + Utils.globexToRegex(nameFilter) + '$');
    } else {
      namePattern = ANY;
    }

    // State filter
    Thread.State state = null;
    if (stateFilter != null) {
      try {
        state = Thread.State.valueOf(stateFilter.toUpperCase());
      } catch (IllegalArgumentException iae) {
        throw new ScriptException("Invalid state filter $stateFilter", iae);
      }
    }

    //
    Map<String, Thread> threads = getThreads();
    threads.each() {
      if (it != null) {
        def nameMatcher = it.value.name =~ namePattern;
        def groupMatcher = it.value.threadGroup.name =~ groupPattern;
        def thread = it.value;
        if (nameMatcher.matches() && groupMatcher.matches() && (state == null || it.value.state == state)) {
          try {
            context.provide(thread)
          }
          catch (IOException e) {
            e.printStackTrace()
          };
        }
      }
    }
  }

  @Usage("interrupt vm threads")
  @Man("Interrup VM threads.")
  @Command
  public Pipe<Thread, Thread> interrupt(@Argument @Usage("the thread ids to interrupt") List<Thread> threads) {
    return new Pipe<Thread, Thread>() {
      void open() throws org.crsh.command.ScriptException {
        threads.each(this.&provide)
      }
      void provide(Thread element) throws IOException {
        element.interrupt();
        context.provide(element);
      }
    }
  }

  @Usage("stop vm threads")
  @Man("Stop VM threads.")
  @Command
  public Pipe<Thread, Thread> stop(@Argument @Usage("the thread ids to stop") List<Thread> threads) {
    return new Pipe<Thread, Thread>() {
      void open() throws org.crsh.command.ScriptException {
        threads.each(this.&provide)
      }
      void provide(Thread element) throws IOException {
        element.stop();
        context.provide(element);
      }
    }
  }

  @Usage("dump vm threads")
  @Man("Dump VM threads.")
  @Command
  public Pipe<Thread, Object> dump(@Argument @Usage("the thread ids to dump") List<Thread> threads) {
    return new Pipe<Thread, Object>() {
      void open() throws org.crsh.command.ScriptException {
        threads.each(this.&provide)
      }
      void provide(Thread element) throws IOException {
        Exception e = new Exception("Thread ${element.id} stack trace")
        e.setStackTrace(element.stackTrace)
        e.printStackTrace(context.writer)
      }
    }
  }

  static ThreadGroup getRoot() {
    ThreadGroup group = Thread.currentThread().threadGroup;
    ThreadGroup parent;
    while ((parent = group.parent) != null) {
      group = parent;
    }
    return group;
  }

  static Map<String, Thread> getThreads() {
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
