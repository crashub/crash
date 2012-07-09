package org.crsh.shell.ui;

import junit.framework.TestCase;
import org.crsh.text.Color;
import org.crsh.text.Decoration;
import org.crsh.text.Style;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class AnsiBuilderTestCase extends TestCase {

  public void testReset() throws Exception {
    assertEquals("\u001B[0m", Style.reset.toAnsiSequence());
  }

  public void testDecoration() throws Exception {
    assertEquals("\u001B[5m", Style.style(Decoration.blink, null, null).toAnsiSequence());
    assertEquals("\u001B[1m", Style.style(Decoration.bold, null, null).toAnsiSequence());
    assertEquals("\u001B[4m", Style.style(Decoration.underline, null, null).toAnsiSequence());
  }

  public void testForeground() throws Exception {
    assertEquals("\u001B[30m", Style.style(null, Color.black, null).toAnsiSequence());
    assertEquals("\u001B[34m", Style.style(null, Color.blue, null).toAnsiSequence());
    assertEquals("\u001B[36m", Style.style(null, Color.cyan, null).toAnsiSequence());
    assertEquals("\u001B[32m", Style.style(null, Color.green, null).toAnsiSequence());
    assertEquals("\u001B[35m", Style.style(null, Color.magenta, null).toAnsiSequence());
    assertEquals("\u001B[31m", Style.style(null, Color.red, null).toAnsiSequence());
    assertEquals("\u001B[33m", Style.style(null, Color.yellow, null).toAnsiSequence());
    assertEquals("\u001B[37m", Style.style(null, Color.white, null).toAnsiSequence());
  }

  public void testBackground() throws Exception {
    assertEquals("\u001B[40m", Style.style(null, null, Color.black).toAnsiSequence());
    assertEquals("\u001B[44m", Style.style(null, null, Color.blue).toAnsiSequence());
    assertEquals("\u001B[46m", Style.style(null, null, Color.cyan).toAnsiSequence());
    assertEquals("\u001B[42m", Style.style(null, null, Color.green).toAnsiSequence());
    assertEquals("\u001B[45m", Style.style(null, null, Color.magenta).toAnsiSequence());
    assertEquals("\u001B[41m", Style.style(null, null, Color.red).toAnsiSequence());
    assertEquals("\u001B[43m", Style.style(null, null, Color.yellow).toAnsiSequence());
    assertEquals("\u001B[47m", Style.style(null, null, Color.white).toAnsiSequence());
  }

  public void testMany() throws Exception {
    assertEquals("\u001B[34;40m", Style.style(null, Color.blue, Color.black).toAnsiSequence());
    assertEquals("\u001B[4;40m", Style.style(Decoration.underline, null, Color.black).toAnsiSequence());
    assertEquals("\u001B[4;34m", Style.style(Decoration.underline, Color.blue, null).toAnsiSequence());
    assertEquals("\u001B[4;34;40m", Style.style(Decoration.underline, Color.blue, Color.black).toAnsiSequence());
  }
}
