package org.crsh;

import org.crsh.command.ScriptException;
import org.crsh.shell.AbstractCommandTestCase;
import org.crsh.text.Data;

import java.sql.SQLException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class JDBCCommandTestCase extends AbstractCommandTestCase {

  public void testCannotConnect() {
    assertEvalError("jdbc connect jdbc:foo", SQLException.class);
  }

  public void testNotConnected() {
    assertEvalError("jdbc execute create table derbyDB(num int, addr varchar(40))", ScriptException.class);
  }

  public void testExecute() {
    System.setProperty("derby.connection.requireAuthentication", "true");
    System.setProperty("derby.authentication.provider", "BUILTIN");
    System.setProperty("derby.user.my_user", "my_password");
    assertOk("jdbc connect -u my_user -p my_password jdbc:derby:memory:EmbeddedDB;create=true");
    assertOk("jdbc execute create table derbyDB(num int, addr varchar(40))");
    assertOk("jdbc execute insert into derbyDB values (1956,'Webster St.')");
    Data res = assertOk("jdbc execute select * from derbyDb").getData();
    assertTrue(res.contains("Webster"));
    lifeCycle.setCommand("foo", "jdbc.execute 'select * from derbyDb', { out << it['NUM'] }");
    assertOk("1956", "foo");
    assertOk("jdbc close");
  }

  public void testClose() {
    assertEvalError("jdbc close", ScriptException.class);
  }
}
