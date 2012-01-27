/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.crsh.jcr.command;

import java.util.Properties;
import junit.framework.TestCase;


/**
 *
 * @author ehugonnet
 */
public class InitPropertiesTest extends TestCase {
  
  public InitPropertiesTest() {
  }

  public void testGetProperties() {
    InitProperties init = new InitProperties("org.apache.jackrabbit.repository.conf=repository" +
        "-in-memory.xml;org.apache.jackrabbit.repository.home=/home/ehugonnet/tmp/crash/jcr/target" +
        "/test-classes/conf/transient");
    Properties props = init.getProperties();
    assertNotNull(props);
    assertEquals(2, props.size());
    assertEquals("repository-in-memory.xml", props.get("org.apache.jackrabbit.repository.conf"));
    assertEquals("/home/ehugonnet/tmp/crash/jcr/target/test-classes/conf/transient", props.get("org.apache.jackrabbit.repository.home"));
  }
}
