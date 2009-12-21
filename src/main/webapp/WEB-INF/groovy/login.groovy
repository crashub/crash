import org.crsh.shell.ScriptException;
import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.ValueFormatException;

prompt = { ->
  if (session == null)
    return "% ";
  else {
    def node = getCurrentNode();
    return "[" + node.path + "]% ";
  }
}

getCurrentNode = { ->
  assertConnected();
  return findNodeByPath(currentPath);
}

setCurrentNode = { node ->
  currentPath = node.path;
}

assertConnected = { ->
  if (session == null)
    throw new ScriptException("Not connect to a repository");
};

/**
 * Locate a node by its path and returns it. If no path is provided the root node
 * will be returned.
 */
findNodeByPath = { path ->
  def item = findItemByPath(path);
  if (item instanceof Node)
    return (Node)item;
  throw new ScriptException("""Item at $path is a property and not a node""");
};

/**
 * Locate an item by its path and returns it. If no path is provided the root node
 * will be returned. If the path is relative then the item will be resolved against the
 * current node.
 */
findItemByPath = { path ->
  assertConnected();
  if (path == null)
    path = "/";
  if (path.startsWith("/"))
  {
    return session.getItem(path);
  }
  def node = getCurrentNode();
  if (node.hasNode(path))
    return node.getNode(path);
  if (node.hasProperty(path))
    return node.getProperty(path);
  throw new ScriptException("""$path : no such item""");
};

formatValue = { value ->
  switch (value.type)
  {
    case PropertyType.STRING:
      return """'$value.string'""";
    case PropertyType.DATE:
    case PropertyType.LONG:
    case PropertyType.BOOLEAN:
    case PropertyType.PATH:
    case PropertyType.NAME:
      return value.string;
    case PropertyType.REFERENCE:
      def id = value.string;
      def referenced = session.getNodeByUUID(id);
      return """${value.string} -> ${referenced.path}""";
    case PropertyType.BINARY:
      return "<binary>";
    default:
      return """$value.string""";
  }
}

formatPropertyValue = { property ->
  try {
    return formatValue(property.value);
  }
  catch (ValueFormatException e) {
    def s = "[";
    def count = 0;
    property.values.eachWithIndex() { value, i ->
      if (i > 0)
        s += ",";
      s += formatValue(value);
    }
    s += "]";
    return s;
  }
}

safeClose = { closeable ->
  if (closeable != null) {
    try {
      closeable.close();
    }
    catch (Exception ignore) {
    }
  }
}
