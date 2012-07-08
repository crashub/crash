package org.crsh.shell.ui;

import junit.framework.TestCase;
import org.crsh.text.Color;
import org.crsh.text.FormattingData;
import org.crsh.text.Style;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class AnsiBuilderTestCase extends TestCase {

  public void testReset() throws Exception {
    assertEquals("\u001B[0m", new FormattingData(null).asAnsiSequence());
  }

  public void testDecoration() throws Exception {
    assertEquals("\u001B[5m", new FormattingData(new Style(Decoration.blink, null, null)).asAnsiSequence());
    assertEquals("\u001B[1m", new FormattingData(new Style(Decoration.bold, null, null)).asAnsiSequence());
    assertEquals("\u001B[4m", new FormattingData(new Style(Decoration.underline, null, null)).asAnsiSequence());
  }

  public void testForeground() throws Exception {
    assertEquals("\u001B[30m", new FormattingData(new Style(null, Color.black, null)).asAnsiSequence());
    assertEquals("\u001B[34m", new FormattingData(new Style(null, Color.blue, null)).asAnsiSequence());
    assertEquals("\u001B[36m", new FormattingData(new Style(null, Color.cyan, null)).asAnsiSequence());
    assertEquals("\u001B[32m", new FormattingData(new Style(null, Color.green, null)).asAnsiSequence());
    assertEquals("\u001B[35m", new FormattingData(new Style(null, Color.magenta, null)).asAnsiSequence());
    assertEquals("\u001B[31m", new FormattingData(new Style(null, Color.red, null)).asAnsiSequence());
    assertEquals("\u001B[33m", new FormattingData(new Style(null, Color.yellow, null)).asAnsiSequence());
    assertEquals("\u001B[37m", new FormattingData(new Style(null, Color.white, null)).asAnsiSequence());
  }

  public void testBackground() throws Exception {
    assertEquals("\u001B[40m", new FormattingData(new Style(null, null, Color.black)).asAnsiSequence());
    assertEquals("\u001B[44m", new FormattingData(new Style(null, null, Color.blue)).asAnsiSequence());
    assertEquals("\u001B[46m", new FormattingData(new Style(null, null, Color.cyan)).asAnsiSequence());
    assertEquals("\u001B[42m", new FormattingData(new Style(null, null, Color.green)).asAnsiSequence());
    assertEquals("\u001B[45m", new FormattingData(new Style(null, null, Color.magenta)).asAnsiSequence());
    assertEquals("\u001B[41m", new FormattingData(new Style(null, null, Color.red)).asAnsiSequence());
    assertEquals("\u001B[43m", new FormattingData(new Style(null, null, Color.yellow)).asAnsiSequence());
    assertEquals("\u001B[47m", new FormattingData(new Style(null, null, Color.white)).asAnsiSequence());
  }

  public void testMany() throws Exception {
    assertEquals("\u001B[34;40m", new FormattingData(new Style(null, Color.blue, Color.black)).asAnsiSequence());
    assertEquals("\u001B[4;40m", new FormattingData(new Style(Decoration.underline, null, Color.black)).asAnsiSequence());
    assertEquals("\u001B[4;34m", new FormattingData(new Style(Decoration.underline, Color.blue, null)).asAnsiSequence());
    assertEquals("\u001B[4;34;40m", new FormattingData(new Style(Decoration.underline, Color.blue, Color.black)).asAnsiSequence());
  }
}
