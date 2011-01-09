class sleep extends CRaSHCommand
{

  @Description("Sleep for some time")
  @Command
  Object main(@Description("Sleep time in seconds") @Argument int time) throws ScriptException {
    if (time < 0)
      throw new ScriptException("Cannot provide negative time value $time");
    Thread.sleep(time * 1000);
    return null;
  }
}