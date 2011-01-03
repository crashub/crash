class sleep extends CRaSHCommand
{

  @Command(description = "Provides basic help")
  Object main(@Argument(description = "Sleep time in seconds") int time) throws ScriptException {
    if (time < 0)
      throw new ScriptException("Cannot provide negative time value $time");
    Thread.sleep(time * 1000);
    return null;
  }
}