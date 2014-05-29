package crash.commands.test

import org.crsh.cli.Command
import org.crsh.keyboard.KeyHandler
import org.crsh.keyboard.KeyType

class keyhandler implements KeyHandler {

  @Override
  void handle(KeyType type, int[] sequence) {
    if (type == KeyType.CHARACTER && sequence[0] == 'q') {
      out << "done";
      out.flush();
      synchronized (lock) {
        lock.notify();
      }
    } else {
      out << type.name() + " " + Arrays.toString(sequence) + "\n";
      out.flush()
    }
  }

  final Object lock = new Object()

  @Command
  void main() {
    synchronized (lock) {
      // Wait until it is unlocked
      // note that ctrl-c will interrupt the thread too
      lock.wait()
    }
 }
}