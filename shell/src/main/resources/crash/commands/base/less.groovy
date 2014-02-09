package crash.commands.base;

import org.crsh.cli.Usage
import org.crsh.cli.Command
import org.crsh.command.PipeCommand
import org.crsh.console.KeyHandler
import org.crsh.console.KeyType
import org.crsh.text.Chunk

class less
{

  static class impl extends PipeCommand<Chunk, Chunk> implements KeyHandler, Runnable {

    /** . */
    final def buffer = new LinkedList<Chunk>()

    /** . */
    final def lock = new Object()

    /** . */
    def boolean done = false;

    /** . */
    def width = 0;

    @Override
    void handle(KeyType type, int[] sequence) {
      if (type == KeyType.q) {
        synchronized (lock) {
          done = true;
          lock.notifyAll();
        }
      }
    }

    /**
     * This method monitors the changes of the display periodically and refresh the screen if the size changes.
     */
    @Override
    void run() {
      // Refresh the screen periodically based upond needs
      while (true) {

        def w = context.getWidth()
        if (w != width) {
          context.writer.print("width changed");
          context.writer.flush();
          width = w;
        }

        synchronized (lock) {
          lock.wait(1000);
          if (done) {
            break;
          }
        }
      }

      context.writer.print("done");
      context.writer.flush();
    }

    @Override
    void open() throws ScriptException {
      new Thread(this).start();
    }

    @Override
    void provide(Chunk element) throws ScriptException, IOException {
      buffer.addLast(element);
      context.provide(element);
    }

    @Override
    void close() throws ScriptException {
      context.writer.print("waiting now");
      context.writer.flush();
      synchronized (lock) {
        if (!done) {
          lock.wait();
          if (!done) {
            // Interrupted...
            done = true;
            lock.notifyAll();
          }
        }
      }
    }
  }

  @Usage("more...")
  @Command
  PipeCommand<Chunk, Chunk> main() {
    return new impl();
  }
}