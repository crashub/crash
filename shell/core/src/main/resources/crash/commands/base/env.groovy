class env extends CRaSHCommand
{
  @Usage("Display the term width")
  @Command
  Object main(InvocationContext<Void, Void> context) throws ScriptException {
    StringBuilder sb = new StringBuilder();
    sb.append("width: $context.width");
    return sb;
  }
}