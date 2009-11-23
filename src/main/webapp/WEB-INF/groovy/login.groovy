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

findNodeByPath = { path ->
  assertConnected();
  if (path == null)
    path = "/";
  def node;
  if (path.startsWith("/"))
  {
    node = session.getRootNode();
    if (path.equals("/"))
      path = ".";
    else
      path = path.substring(1);
  }
  else
  {
    node = getCurrentNode();
  }
  if (!node.hasNode(path))
    throw new ScriptException("""$path : no such node""");
  return node.getNode(path);
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
      def referenced = session.findNodeByUUID(id);
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
