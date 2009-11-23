{ path ->
  assertConnected();
  def node = findNodeByPath(path);
  node.save();
}