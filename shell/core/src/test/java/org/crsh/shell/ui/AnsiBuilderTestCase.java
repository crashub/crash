package org.crsh.shell.ui;

import junit.framework.TestCase;
import org.crsh.text.Color;
import org.crsh.text.Style;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class AnsiBuilderTestCase extends TestCase {

  public void testReset() throws Exception {
    assertEquals("\u001B[0m", Style.reset.toAnsiSequence());
  }

  public void testDecoration() throws Exception {
    assertEquals("\u001B[5m", Style.create(Decoration.blink, null, null).toAnsiSequence());
    assertEquals("\u001B[1m", Style.create(Decoration.bold, null, null).toAnsiSequence());
    assertEquals("\u001B[4m", Style.create(Decoration.underline, null, null).toAnsiSequence());
  }

  public void testForeground() throws Exception {
    assertEquals("\u001B[30m", Style.create(null, null, Color.black).toAnsiSequence());
    assertEquals("\u001B[34m", Style.create(null, null, Color.blue).toAnsiSequence());
    assertEquals("\u001B[36m", Style.create(null, null, Color.cyan).toAnsiSequence());
    assertEquals("\u001B[32m", Style.create(null, null, Color.green).toAnsiSequence());
    assertEquals("\u001B[35m", Style.create(null, null, Color.magenta).toAnsiSequence());
    assertEquals("\u001B[31m", Style.create(null, null, Color.red).toAnsiSequence());
    assertEquals("\u001B[33m", Style.create(null, null, Color.yellow).toAnsiSequence());
    assertEquals("\u001B[37m", Style.create(null, null, Color.white).toAnsiSequence());
  }

  public void testBackground() throws Exception {
    assertEquals("\u001B[40m", Style.create(null, Color.black, null).toAnsiSequence());
    assertEquals("\u001B[44m", Style.create(null, Color.blue, null).toAnsiSequence());
    assertEquals("\u001B[46m", Style.create(null, Color.cyan, null).toAnsiSequence());
    assertEquals("\u001B[42m", Style.create(null, Color.green, null).toAnsiSequence());
    assertEquals("\u001B[45m", Style.create(null, Color.magenta, null).toAnsiSequence());
    assertEquals("\u001B[41m", Style.create(null, Color.red, null).toAnsiSequence());
    assertEquals("\u001B[43m", Style.create(null, Color.yellow, null).toAnsiSequence());
    assertEquals("\u001B[47m", Style.create(null, Color.white, null).toAnsiSequence());
  }

  public void testMany() throws Exception {
    assertEquals("\u001B[34;40m", Style.create(null, Color.black, Color.blue).toAnsiSequence());
    assertEquals("\u001B[4;40m", Style.create(Decoration.underline, Color.black, null).toAnsiSequence());
    assertEquals("\u001B[4;34m", Style.create(Decoration.underline, null, Color.blue).toAnsiSequence());
    assertEquals("\u001B[4;34;40m", Style.create(Decoration.underline, Color.black, Color.blue).toAnsiSequence());
  }
}
