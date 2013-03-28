import org.crsh.cli.descriptor.ParameterDescriptor
import org.crsh.cli.spi.Completer
import org.crsh.cli.spi.Completion

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.util.regex.Pattern;
import org.crsh.cli.Usage
import org.crsh.cli.Command
import org.crsh.command.InvocationContext
import org.crsh.cli.Option
import org.crsh.cli.Man
import org.crsh.cli.Argument

import org.crsh.command.PipeCommand
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

In addition of the classical usage, the various commands (ls, stop, interrupt) can be combined
with a pipe, the most common operation is to combine the ls command with the stop, interrupt or dump command,
for instance the following command will interrupt all the thread having a name starting with the 'pool' prefix:

% thread ls --filter pool.* | thread interrupt
Interrupted thread Thread[pool-1-thread-1,5,main]
Interrupted thread Thread[pool-1-thread-2,5,main]
Interrupted thread Thread[pool-1-thread-3,5,main]
Interrupted thread Thread[pool-1-thread-4,5,main]
Interrupted thread Thread[pool-1-thread-5,5,main]""")
public class thread  implements Completer {

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
      while (!Thread.interrupted()) {
        out.cls()
        out.show(table);
        out.flush();
        Thread.sleep(1000);
      }
    }
    finally {
      context.releaseAlternateBuffer();
    }
  }

  private static final Pattern ANY = Pattern.compile(".*");

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

  @Usage("produces threads for pipe")
  @Man("Produces VM threads from ids (use with pipe to dump or interrupt them). eg 'thread produce 12 | thread dump'")
  @Command
  public void produce(
          InvocationContext<Thread> context,
          @ThreadId @Argument @Usage("the thread ids to produces") List<String> ids) {
    ids.each { id ->
      def t = getThreads()[id]
      if (t != null) {
        context.provide(t);
      }
    }
  }

  @Usage("interrupt vm threads")
  @Man("Interrup VM threads.")
  @Command
  public PipeCommand<Thread, Object> interrupt(@Argument @Usage("the thread ids to interrupt") List<String> ids) {
/*
    apply(context, ids, {
      it.interrupt();
      context.writer.println("Interrupted thread $it");
    })
*/
    return new PipeCommand<Thread, Object>() {
      void provide(Thread element) throws IOException {
        element.interrupt();
      }
    }
  }

  @Usage("stop vm threads")
  @Man("Stop VM threads.")
  @Command
  public PipeCommand<Thread, Object> stop(@Argument @Usage("the thread ids to stop") List<String> ids) {
/*
    apply(context, ids, {
      it.stop();
      context.writer.println("Stopped thread $it");
    })
*/
    return new PipeCommand<Thread, Object>() {
      void provide(Thread element) throws IOException {
        element.stop();
      }
    }
  }

  @Usage("dump vm threads")
  @Man("Dump VM threads.")
  @Command
  public PipeCommand<Thread, Object> dump(@Argument @Usage("the thread ids to dump") List<String> ids) {
/*
    apply(context, ids, {
      Exception e = new Exception("Thread ${it.id} stack trace")
      e.setStackTrace(it.stackTrace)
      e.printStackTrace(context.writer)
    })
*/
    return new PipeCommand<Thread, Object>() {
      void provide(Thread element) throws IOException {
        Exception e = new Exception("Thread ${element.id} stack trace")
        e.setStackTrace(element.stackTrace)
        e.printStackTrace(context.writer)
      }
    }
  }

    Completion complete(ParameterDescriptor parameter, String prefix) throws Exception {
        def b = new Completion.Builder(prefix);
        if (parameter.getAnnotation().annotationType().equals(ThreadId.class)) {
            getThreads().each() { k, thread ->
            if (thread.id.toString().startsWith(prefix)) {
              b.add(thread.id.toString().substring(prefix.length()), true)
            }
          }
        }
        return b.build();
    }
/*
  public void apply(InvocationContext<Thread, Void> context, List<String> ids, Closure closure) {
    if (context.piped) {
      context.consumer().each(closure)
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
*/

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

@Retention(RetentionPolicy.RUNTIME)
@Usage("the thread ids")
@Man("The ids of the thread")
@Argument(name = "ids")
@interface ThreadId { }

