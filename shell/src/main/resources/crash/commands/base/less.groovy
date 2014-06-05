package crash.commands.base

import org.crsh.cli.Man;
import org.crsh.cli.Usage
import org.crsh.cli.Command
import org.crsh.command.Pipe
import org.crsh.keyboard.KeyHandler
import org.crsh.keyboard.KeyType
import org.crsh.text.Screenable
import org.crsh.text.ScreenContext
import org.crsh.text.CLS
import org.crsh.text.VirtualScreen
import org.crsh.text.Style

class less
{

  static class impl extends Pipe<CharSequence, Object> implements KeyHandler, ScreenContext {

    /** . */
    VirtualScreen buffer;

    /** . */
    final def lock = new Object()

    /** . */
    def boolean done = false;

    impl() {
    }

    @Override
    void handle(KeyType type, int[] sequence) {
      if (type == KeyType.CHARACTER && sequence[0] == 'q') {
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
        } else if (type == KeyType.CHARACTER && sequence[0] == ' ') {
          buffer.nextPage();
        }
        buffer.paint();
        buffer.flush();
      }
    }

    int getWidth() {
      return context.getWidth();
    }

    int getHeight() {
      return context.getHeight();
    }

    Appendable append(char c) throws IOException {
      buffer.append(c);
      return this;
    }

    Appendable append(CharSequence s) throws IOException {
      buffer.append(s);
      return this;
    }

    Appendable append(CharSequence csq, int start, int end) throws IOException {
      buffer.append(csq, start, end);
      return this;
    }

    Screenable append(Style style) throws IOException {
      buffer.append(style);
      return this;
    }

    Screenable cls() throws IOException {
      buffer.cls();
      return this;
    }

    void open() throws ScriptException {
      buffer = new VirtualScreen(context);
      context.takeAlternateBuffer();
    }

    @Override
    void provide(CharSequence element) throws ScriptException, IOException {
      if (element instanceof CLS) {
        buffer.cls();
      } else if (element instanceof Style) {
        buffer.append(element);
      } else {
        CharSequence s;
        if (element instanceof CharSequence) {
          s = (CharSequence)element;
        } else {
          s = element.toString();
        }
        buffer.append(s);
      }
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
  Pipe<CharSequence, Object> main() {
    return new impl();
  }
}