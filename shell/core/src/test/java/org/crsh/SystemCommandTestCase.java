package org.crsh;

import org.crsh.shell.AbstractCommandTestCase;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class SystemCommandTestCase extends AbstractCommandTestCase {

  public void testFoo() throws Exception {
    System.setProperty("foo", "bar");
    lifeCycle.setCommand("ls", "system.propls filter:'foo', { out << it.value }");
    assertOk("bar", "ls");
  }
}
