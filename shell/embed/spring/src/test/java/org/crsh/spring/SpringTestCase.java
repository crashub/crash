package org.crsh.spring;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.crsh.BaseProcessContext;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellFactory;
import org.crsh.shell.ShellProcess;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.UrlResource;

import java.net.URL;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class SpringTestCase extends TestCase {

  public void testFoo() throws Exception {

    URL xml = SpringTestCase.class.getResource("spring.xml");
    Assert.assertNotNull(xml);

    //
    GenericXmlApplicationContext context = new GenericXmlApplicationContext(new UrlResource(xml));
    context.start();

    //
    SpringBootstrap bootstrap = context.getBean(SpringBootstrap.class);

    // Test a bit
    ShellFactory factory = bootstrap.getContext().getPlugin(ShellFactory.class);
    Shell shell = factory.create(null);
    assertNotNull(shell);
    ShellProcess process = shell.createProcess("telnet");
    assertNotNull(process);
    BaseProcessContext pc = BaseProcessContext.create(process);
    String r = pc.execute().getResponse().getText();
    assertEquals("2", r);
  }
}
