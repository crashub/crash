welcome = { ->
  def hostName;
  try {
    hostName = java.net.InetAddress.getLocalHost().getHostName();
  } catch (java.net.UnknownHostException ignore) {
    hostName = "localhost";
  }
  return """\
   ______
 .~      ~. |`````````,       .'.                   ..'''' |         |
|           |'''|'''''      .''```.              .''       |_________|
|           |    `.       .'       `.         ..'          |         |
 `.______.' |      `.   .'           `. ....''             |         | ${shellContext.version}

Follow and support the project on http://crsh.googlecode.com
Welcome to $hostName + !
It is ${new Date()} now""";
}

prompt = { ->
  return "% ";
}
