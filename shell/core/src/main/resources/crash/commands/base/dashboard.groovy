import org.crsh.text.ui.UIBuilder

def table = new UIBuilder().table(columns: [1], rows: [1,1]) {
  header {
    table(columns:[1]) {
      header(bold: true, fg: black, bg: white) {
        label("top");
      }
      row {
        execute {
          thread.ls();
        }
      }
    }
  }
  header {
    table(columns: [1,1,1], separator: dashed, cellRightPadding: 1) {
      header(bold: true, fg: black, bg: white) {
        label("props");
        label("env");
        label("jvm");
      }
      row {
        execute {
          eval("system propls -f java.*")
        }
        execute {
          eval("env")
        }
        table(columns: [1,2]) {
          row() {
            label("Heap:")
            execute {
              eval("jvm heap")
            }
          }
          row() {
            label("Non heap:")
            execute {
              eval("jvm nonheap")
            }
          }
          jvm.pools { name ->
            row() {
              label("$name:")
              execute {
                eval("jvm pool '$name'")
              }
            }
          }
        }
      }
    }
  }
}

while (!Thread.interrupted()) {
  out.cls()
  out.show(table);
  out.flush();
  Thread.sleep(1000);
}
