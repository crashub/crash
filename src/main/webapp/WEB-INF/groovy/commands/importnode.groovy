import org.crsh.shell.ScriptException;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.util.Calendar;
import org.crsh.console.ConsoleBuilder;
import javax.jcr.ImportUUIDBehavior;

/*
 * Exports a node to a JCR file.
 */
{ String srcPath, String dstPath ->
  assertConnected();

  // Source node to export
  def srcNode = findNodeByPath(srcPath);

  //
  if (srcNode.primaryNodeType.name != "nt:file")
    throw new ScriptException("Can only import file");

  // Get content
  def data = srcNode["jcr:content"]["jcr:data"];

  //
  def dstNode = findNodeByPath(dstPath);

  //
  srcNode.session.importXML(dstPath, data, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);

  //
  return "Node imported";
}