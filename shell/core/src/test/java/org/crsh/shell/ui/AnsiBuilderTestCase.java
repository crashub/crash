package org.crsh.shell.ui;

import junit.framework.TestCase;
import org.crsh.term.ANSIFontBuilder;
import org.crsh.text.Color;
import org.crsh.text.FormattingData;
import org.crsh.text.Style;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class AnsiBuilderTestCase extends TestCase {

  public void testReset() throws Exception {
    ANSIFontBuilder ansiFontBuilder = new ANSIFontBuilder();
    
    assertEquals("\u001B[0m", ansiFontBuilder.build(new FormattingData(null)));

  }

  public void testDecoration() throws Exception {
    ANSIFontBuilder ansiFontBuilder = new ANSIFontBuilder();
  
    assertEquals("\u001B[5m", ansiFontBuilder.build(new FormattingData(new Style(Decoration.blink, null, null))));
    assertEquals("\u001B[1m", ansiFontBuilder.build(new FormattingData(new Style(Decoration.bold, null, null))));
    assertEquals("\u001B[4m", ansiFontBuilder.build(new FormattingData(new Style(Decoration.underline, null, null))));

  }

  public void testForeground() throws Exception {
    ANSIFontBuilder ansiFontBuilder = new ANSIFontBuilder();

    assertEquals("\u001B[30m", ansiFontBuilder.build(new FormattingData(new Style(null, Color.black, null))));
    assertEquals("\u001B[34m", ansiFontBuilder.build(new FormattingData(new Style(null, Color.blue, null))));
    assertEquals("\u001B[36m", ansiFontBuilder.build(new FormattingData(new Style(null, Color.cyan, null))));
    assertEquals("\u001B[32m", ansiFontBuilder.build(new FormattingData(new Style(null, Color.green, null))));
    assertEquals("\u001B[35m", ansiFontBuilder.build(new FormattingData(new Style(null, Color.magenta, null))));
    assertEquals("\u001B[31m", ansiFontBuilder.build(new FormattingData(new Style(null, Color.red, null))));
    assertEquals("\u001B[33m", ansiFontBuilder.build(new FormattingData(new Style(null, Color.yellow, null))));
    assertEquals("\u001B[37m", ansiFontBuilder.build(new FormattingData(new Style(null, Color.white, null))));

  }

  public void testBackground() throws Exception {
    ANSIFontBuilder ansiFontBuilder = new ANSIFontBuilder();

    assertEquals("\u001B[40m", ansiFontBuilder.build(new FormattingData(new Style(null, null, Color.black))));
    assertEquals("\u001B[44m", ansiFontBuilder.build(new FormattingData(new Style(null, null, Color.blue))));
    assertEquals("\u001B[46m", ansiFontBuilder.build(new FormattingData(new Style(null, null, Color.cyan))));
    assertEquals("\u001B[42m", ansiFontBuilder.build(new FormattingData(new Style(null, null, Color.green))));
    assertEquals("\u001B[45m", ansiFontBuilder.build(new FormattingData(new Style(null, null, Color.magenta))));
    assertEquals("\u001B[41m", ansiFontBuilder.build(new FormattingData(new Style(null, null, Color.red))));
    assertEquals("\u001B[43m", ansiFontBuilder.build(new FormattingData(new Style(null, null, Color.yellow))));
    assertEquals("\u001B[47m", ansiFontBuilder.build(new FormattingData(new Style(null, null, Color.white))));

  }

  public void testMany() throws Exception {
    ANSIFontBuilder ansiFontBuilder = new ANSIFontBuilder();

    assertEquals("\u001B[34;40m", ansiFontBuilder.build(new FormattingData(new Style(null, Color.blue, Color.black))));
    assertEquals("\u001B[4;40m", ansiFontBuilder.build(new FormattingData(new Style(Decoration.underline, null, Color.black))));
    assertEquals("\u001B[4;34m", ansiFontBuilder.build(new FormattingData(new Style(Decoration.underline, Color.blue, null))));
    assertEquals("\u001B[4;34;40m", ansiFontBuilder.build(new FormattingData(new Style(Decoration.underline, Color.blue, Color.black))));

  }

}
