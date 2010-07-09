import org.crsh.command.ScriptException;
import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.ValueFormatException;

welcome = { ->
    def hostName;
    try {
      hostName = java.net.InetAddress.getLocalHost().getHostName();
    } catch (java.net.UnknownHostException ignore) {
      hostName = "localhost";
    }
    String ret =
    "CRaSH " + version + " (http://crsh.googlecode.com)\r\n" +
    "Welcome to " + hostName + "!\r\n" +
    "It is " + new Date() + " now.\r\n" +
    "% ";
    return ret;
}

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
 * @throws ScriptException if the item cannot be found
 */
findItemByPath = { path ->
  assertConnected();
  if (path == null)
    path = "/";
  def item = getItemByPath(path);
  if (item != null)
    return item;
  throw new ScriptException("""$path : no such item""");
};

/**
 * Locate an item by its path and returns it. If no path is provided the root node
 * will be returned. If the path is relative then the item will be resolved against the
 * current node.
 * @throws ScriptException if no path is provided
 */
getItemByPath = { path ->
  assertConnected();
  if (path == null)
    throw new ScriptException("No path provided");
  if (path.startsWith("/"))
  {
    return session.getItem(path);
  }
  def node = getCurrentNode();
  if (node.hasNode(path))
    return node.getNode(path);
  if (node.hasProperty(path))
    return node.getProperty(path);
  return null;
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

formatNode = { builder, n, nodeDepth, propertiesDepth ->
  builder.node(n.path) {
    if (propertiesDepth > 0) {
      node('properties') {
        n.properties.each() { property ->
        label(property.name + ": " + formatPropertyValue(property));
      }
    }
    if (nodeDepth > 0) {
      node('children') {
        n.each { child ->
          formatNode(builder, child, nodeDepth -1, propertiesDepth -1);  
        }
      }
    }
  }
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
