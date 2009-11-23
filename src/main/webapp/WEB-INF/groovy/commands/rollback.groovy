{ path ->
  assertConnected();
  def node = findNodeByPath(path);
  node.refresh(false);
}