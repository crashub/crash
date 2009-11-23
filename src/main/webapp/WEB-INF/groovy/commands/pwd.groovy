import org.crsh.console.ConsoleBuilder;
{ ->
  assertConnected();
  def builder = new ConsoleBuilder();
  builder.message(currentPath);
  return builder;
}