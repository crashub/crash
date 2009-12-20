import org.crsh.shell.ScriptException;
{ String path ->
  assertConnected();
  def currentNode = getCurrentNode();
  if (currentNode.hasNode(path))
  {
    def node = currentNode.getNode(path);
    node.remove();
  }
  else if (currentNode.hasProperty(path))
  {
    def property = currentNode.getProperty(path);
    property.remove();
  }
  else
    throw new ScriptException("""rm: $path: No such node or property""");
  return null;
}