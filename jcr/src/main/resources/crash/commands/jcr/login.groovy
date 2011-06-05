import org.crsh.command.ScriptException;
import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.ValueFormatException
import org.crsh.jcr.command.Path;

getCurrentNode = { ->
  assertConnected();
  return findNodeByPath(currentPath);
}

setCurrentNode = { node ->
  currentPath = new Path(node.path);
}

assertConnected = { ->
  if (session == null)
    throw new ScriptException("Not connect to a repository");
};

/**
 * Locates a node by its path and returns it.
 * If no path is provided the root node will be returned.
 * If the path is relative then the node will be resolved against the current node.
 * @throws ScriptException when the provided path does not point to a valid node
 */
findNodeByPath = { Path path ->
  def item = findItemByPath(path);
  if (item instanceof Node)
    return item;
  throw new ScriptException("""Item at $path is a property and not a node""");
};

/**
 * Locates an item by its path and returns it.
 * If no path is provided the root node will be returned.
 * If the path is relative then the item will be resolved lrea the current node.
 * @throws ScriptException when the provided path does not point to a valid item
 */
findItemByPath = { Path path ->
  assertConnected();
  if (path == null)
    path = Path.ROOT;
  def item = getItemByPath(path);
  if (item != null)
    return item;
  throw new ScriptException("""$path.string : no such item""");
};

/**
 * Locates a node by its path and returns it.
 * When no node is found, the null value is returned.
 * If the path is relative then the node will be resolved against the current node.
 * It calls the getItemByPath function and makes sure the returned
 * item is a node.
 * @throws ScriptException if no path is provided
 * @throws ScriptException when the provided path does not point to a valid node
 */
getNodeByPath = { Path path ->
  def item = getItemByPath(path);
  if (item == null) {
    return null;
  } else if (item instanceof Node) {
    return item;
  } else {
    throw new ScriptException("The path $path.string is an item instead of a node");
  }
}

/**
 * Locates an item by its path and returns it.
 * When no item is found, the null value is returned.
 * If the path is relative then the item will be resolved against the current node.
 * @throws ScriptException if no path is provided
 */
getItemByPath = { Path path ->
  assertConnected();
  if (path == null)
    throw new ScriptException("No path provided");
  if (path.isAbsolute())
    return session.getItem(path.string);
  def node = getCurrentNode();
  if (node.hasNode(path.string))
    return node.getNode(path.string);
  if (node.hasProperty(path.string))
    return node.getProperty(path.string);
  return null;
};

/**
 * Compute the absolute path from the provided path argument.
 * If the path is absolute then it is returned.
 * If the path is relative then the absolute path is computed based on the current active node.
 * @param path the path
 * @return the corresponding absolute path
 * @throws ScriptException if the path argument is null
 */
absolutePath = { Path path ->
  if (path == null)
    throw new ScriptException("No null path accepted");
  def parent;
  if (path.isAbsolute()) {
    parent = session.rootNode;
    path = new Path(path.string.substring(1));
  } else {
    parent = getCurrentNode();
  }
  def parentPath = parent.path;
  if (parentPath == "/") {
    return new Path("/" + path.string);
  } else {
    return new Path(parent.path + "/" + path.string);
  }
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
