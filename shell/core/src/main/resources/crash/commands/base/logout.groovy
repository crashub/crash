if (connection != null) {
  org.crsh.util.Safe.close((java.sql.Connection)connection);
  connection = null;
}