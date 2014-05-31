welcome = { ->
  def hostName;
  try {
    hostName = java.net.InetAddress.getLocalHost().getHostName();
  } catch (java.net.UnknownHostException ignore) {
    hostName = "localhost";
  }
  return """\

   _____     ________                 _______    ____ ____
 .'     `.  |        `.             .'       `. |    |    | ${crash.context.version}
|    |    | |    |    |  .-------.  |    |    | |    |    |
|    |____| |    `   .' |   _|    |  .    '~_ ` |         |
|    |    | |    .   `.  .~'      | | `~_    `| |         |
|    |    | |    |    | |    |    | |    |    | |    |    |
 `._____.'  |____|____| `.________|  `._____.'  |____|____|

Follow and support the project on http://www.crashub.org
Welcome to $hostName + !
It is ${new Date()} now
""";
}

prompt = { ->
  return "% ";
}
