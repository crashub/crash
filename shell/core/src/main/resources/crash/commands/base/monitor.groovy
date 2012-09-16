while (!Thread.interrupted()) {
  out.cls()
  jmx.attributes "java.lang:type=OperatingSystem"
  out.flush();
  Thread.sleep(1000);
}