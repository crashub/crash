import org.crsh.text.ui.UIBuilder

def table = new UIBuilder().table(columns: [1], rows: [1,1]) {
  header {
    table(columns:[1]) {
      header(bold: true, fg: black, bg: white) {
        label("top");
      }
      row{
        eval {
          thread.ls();
        }
      }
    }
  }
  header {
    table(columns: [1,2,1], separator: dashed) {
      header(bold: true, fg: black, bg: white) {
        label("props");
        label("env");
        label("jvm");
      }
      row {
        eval {
          system.propls f:'java.vm.*';
        }
        eval {
          context.provide(Thread.currentThread())
        }
        eval {
          jvm.system();
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
