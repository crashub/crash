class sleep extends CRaSHCommand {
  @Usage("Sleep for some time")
  @Command
  Object main(@Usage("Sleep time in seconds") @Argument int time) throws ScriptException {
    if (time < 0)
      throw new ScriptException("Cannot provide negative time value $time");
    Thread.sleep(time * 1000);
    return null;
  }
}