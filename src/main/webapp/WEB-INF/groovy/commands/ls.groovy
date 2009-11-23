import org.crsh.console.ConsoleBuilder;
{ String path ->
  assertConnected();

  //
  def node = path == null ? getCurrentNode() : findNodeByPath(path);

  //
  def properties = node.getProperties();
  def children = node.getNodes();

  //
  def builder = new ConsoleBuilder();

  //
  builder.table {
    node.eachProperty { property ->
      row([property.name, " ", formatPropertyValue(property)])
    }
  };

  //
  builder.table {
    node.each { child ->
      row([child.name])
    }
  };

  //
  return builder;
}