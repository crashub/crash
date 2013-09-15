package org.crsh.web.servlet;

import org.crsh.plugin.WebPluginLifeCycle;
import org.crsh.vfs.FS;
import org.crsh.vfs.Path;

import javax.servlet.ServletContext;
import javax.servlet.annotation.WebListener;
import java.lang.reflect.UndeclaredThrowableException;

/** @author Julien Viet */
@WebListener
public class WSLifeCycle extends WebPluginLifeCycle {

  @Override
  protected FS createCommandFS(ServletContext context) {
    try {
      return super.createCommandFS(context).mount(Thread.currentThread().getContextClassLoader(), Path.get("/crash/commands/"));
    }
    catch (Exception e) {
      throw new UndeclaredThrowableException(e);
    }
  }

  @Override
  protected FS createConfFS(ServletContext context) {
    try {
      return super.createConfFS(context).mount(Thread.currentThread().getContextClassLoader(), Path.get("/crash/"));
    }
    catch (Exception e) {
      throw new UndeclaredThrowableException(e);
    }
  }
}
