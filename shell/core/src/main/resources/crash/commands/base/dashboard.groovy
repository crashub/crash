import org.crsh.text.ui.UIBuilder

while (!Thread.interrupted()) {
  out.cls()
  UIBuilder ui = new UIBuilder()
  ui.table(weights: [3,1,1,1], border: dashed) {
    header(bold: true, fg: black, bg: white) {
      label("top");
      label("props");
      label("env");
      label("jvm");
    }
    row {
      eval {
        thread.ls();
      }
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
  out << ui;

  out.flush();
  Thread.sleep(1000);
}
