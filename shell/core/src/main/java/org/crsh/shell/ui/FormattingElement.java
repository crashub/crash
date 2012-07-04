package org.crsh.shell.ui;

import org.crsh.shell.io.ShellWriter;
import org.crsh.term.FormattingData;

import java.io.IOException;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class FormattingElement extends Element {

  /** . */
  private Style style;

  public FormattingElement(Style style) {
    this.style = style;
  }
  
  @Override
  void doPrint(UIWriterContext ctx, ShellWriter writer) throws IOException {
    writer.append(ctx, new FormattingData(style));
  }

  @Override
  int width() {
    return 0;
  }
  
}
