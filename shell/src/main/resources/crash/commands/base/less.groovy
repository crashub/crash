package crash.commands.base

import org.crsh.cli.Man;
import org.crsh.cli.Usage
import org.crsh.cli.Command
import org.crsh.command.Pipe
import org.crsh.console.KeyHandler
import org.crsh.console.KeyType
import org.crsh.text.Chunk
import org.crsh.text.ScreenBuffer

class less
{

  static class impl extends Pipe<Chunk, Chunk> implements KeyHandler {

    /** . */
    ScreenBuffer buffer;

    /** . */
    final def lock = new Object()

    /** . */
    def boolean done = false;

    impl() {
    }

    @Override
    void handle(KeyType type, int[] sequence) {
      if (type == KeyType.q) {
        synchronized (lock) {
          done = true;
          lock.notifyAll();
        }
      } else {
        buffer.update();
        if (type == KeyType.DOWN) {
          buffer.nextRow();
        } else if (type == KeyType.UP) {
          buffer.previousRow();
        } else if (type == KeyType.SPACE) {
          buffer.nextPage();
        }
        buffer.paint();
        buffer.flush();
      }
    }

    @Override
    void open() throws ScriptException {
      buffer = new ScreenBuffer(context);
      context.takeAlternateBuffer();
    }

    @Override
    void provide(Chunk element) throws ScriptException, IOException {
      buffer.write(element);
      boolean flush = buffer.update();
      buffer.paint();
      if (flush) {
        buffer.flush();
      }
    }

    @Override
    void flush() throws ScriptException, IOException {
      buffer.update();
      buffer.paint();
      buffer.flush();
    }

    @Override
    void close() throws ScriptException {
      context.takeAlternateBuffer();
      while (!Thread.currentThread().isInterrupted() && !done) {
        buffer.update();
        buffer.paint();
        buffer.flush();
        synchronized (lock) {
          try {
            lock.wait(100);
          }
          catch (InterruptedException e) {
            // Reset interrupted status
            Thread.currentThread().interrupt();
          }
        }
      }
      context.releaseAlternateBuffer();
    }
  }

  @Usage("opposite of more")
  @Man("""\
Less  is a program similar to more, but which allows backward movement in the file as well as forward movement.

The following commands are available while less is running:

SPACE - Scroll forward one page
UP    - Scroll forward one line
DOWN  - Scroll backward one line
q     - Quit""")
  @Command
  Pipe<Chunk, Chunk> main() {
    return new impl();
  }
}