import org.crsh.console.ConsoleBuilder;
{ String path ->
  assertConnected();
  def node = findNodeByPath(path);
  setCurrentNode(node);
}
