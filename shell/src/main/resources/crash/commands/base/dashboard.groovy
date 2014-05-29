package crash.commands.base

import org.crsh.cli.Command
import org.crsh.cli.Usage
import org.crsh.keyboard.KeyHandler
import org.crsh.keyboard.KeyType
import org.crsh.text.ui.UIBuilder

public class dashboard implements KeyHandler {

  @Override
  void handle(KeyType type, int[] sequence) {
    if (type == KeyType.CHARACTER && sequence[0] == 'q') {
      current?.interrupt();
    }
  }

  /** . */
  private volatile Thread current;

  @Command
  @Usage("a monitoring dashboard")
  public void main() {
    def table = new UIBuilder().table(columns: [1], rows: [1,1]) {
      header {
        table(columns:[1]) {
          header(bold: true, fg: black, bg: white) {
            label("top");
          }
          row {
            eval {
              thread.ls();
            }
          }
        }
      }
      header {
        table(columns: [1,1,1], separator: dashed, rightCellPadding: 1) {
          header(bold: true, fg: black, bg: white) {
            label("props");
            label("env");
            label("jvm");
          }
          row {
            eval {
              execute("system propls -f java.*")
            }
            eval {
              execute("env")
            }
            table(columns: [1,2]) {
              row() {
                label("Heap:")
                eval {
                  execute("jvm heap")
                }
              }
              row() {
                label("Non heap:")
                eval {
                  execute("jvm nonheap")
                }
              }
              (jvm.pools | { name ->
                row() {
                  label("$name:")
                  eval {
                    execute("jvm pool '$name'")
                  }
                }
                null
              })()
            }
          }
        }
      }
    }

    context.takeAlternateBuffer();
    current = Thread.currentThread();
    try {
      run = true;
      while (!Thread.currentThread().isInterrupted()) {
        out.cls()
        out.show(table);
        out.flush();
        Thread.sleep(1000);
      }
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt()
    }
    finally {
      context.releaseAlternateBuffer();
    }
  }
}
