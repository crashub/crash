{ ->
  assertConnected();
  session.logout();
  session = null;
  currentPath = null;
  return "Disconnected from workspace";
}