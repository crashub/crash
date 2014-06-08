/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.crsh.command.base;

import org.crsh.command.ScriptException;
import org.crsh.shell.AbstractShellTestCase;

import java.sql.SQLException;

public class JDBCCommandTestCase extends AbstractShellTestCase {

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
    String res = assertOk("jdbc select * from derbyDb");
    assertTrue("Was expecting " + res + " to contain 'Webster'", res.contains("Webster"));
    lifeCycle.bindGroovy("foo", "(jdbc.select { '* from derbyDb' } | { Map it -> it['NUM'] })()");
    assertEquals("1956", assertOk("foo"));
    assertOk("jdbc close");
  }

  public void testClose() {
    assertEvalError("jdbc close", ScriptException.class);
  }
}
