package org.crsh.spring;

import org.crsh.vfs.FS;
import org.crsh.vfs.spi.servlet.ServletContextDriver;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.net.URISyntaxException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class SpringWebBootstrap extends SpringBootstrap implements ServletContextAware {

  /** . */
  private ServletContext servletContext;

  @Override
  protected FS createCommandFS() throws IOException, URISyntaxException {
    FS commandFS = super.createCommandFS();
    if (servletContext != null) {
      commandFS.mount(new ServletContextDriver(servletContext), "/WEB-INF/crash/commands/");
    }
    return commandFS;
  }

  public void setServletContext(ServletContext servletContext) {
    this.servletContext = servletContext;
  }
}
