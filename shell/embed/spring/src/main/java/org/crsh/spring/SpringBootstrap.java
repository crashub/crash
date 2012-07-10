package org.crsh.spring;

import org.crsh.auth.JaasAuthenticationPlugin;
import org.crsh.auth.SimpleAuthenticationPlugin;
import org.crsh.plugin.CRaSHPlugin;
import org.crsh.plugin.PluginContext;
import org.crsh.plugin.PluginDiscovery;
import org.crsh.plugin.PluginLifeCycle;
import org.crsh.plugin.SimplePluginDiscovery;
import org.crsh.processor.term.ProcessorIOHandler;
import org.crsh.shell.impl.command.CRaSHShellFactory;
import org.crsh.vfs.FS;
import org.crsh.vfs.Path;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class SpringBootstrap extends PluginLifeCycle implements
    BeanClassLoaderAware,
    BeanFactoryAware,
    InitializingBean,
    DisposableBean {

  /** . */
  private ClassLoader loader;

  /** . */
  private BeanFactory factory;

  public SpringBootstrap() {
  }

  public void setBeanClassLoader(ClassLoader loader) {
    this.loader = loader;
  }

  public void setBeanFactory(BeanFactory factory) throws BeansException {
    this.factory = factory;
  }

  public void afterPropertiesSet() throws Exception {

    // Add built in plugins
    Collection<CRaSHPlugin> plugins = new LinkedHashSet<CRaSHPlugin>();
    plugins.add(new CRaSHShellFactory());
    plugins.add(new ProcessorIOHandler());
    plugins.add(new JaasAuthenticationPlugin());
    plugins.add(new SimpleAuthenticationPlugin());

    // List beans
    Map<String,Object> attributes;
    if (factory instanceof ListableBeanFactory) {
      ListableBeanFactory listable = (ListableBeanFactory)factory;
      plugins.addAll(listable.getBeansOfType(CRaSHPlugin.class).values());
      attributes = new BeanMap(listable);
    } else {
      attributes = Collections.emptyMap();
    }

    //
    PluginDiscovery discovery = new SimplePluginDiscovery(plugins.toArray(new CRaSHPlugin<?>[plugins.size()]));

    //
    FS cmdFS = new FS();
    cmdFS.mount(loader, Path.get("/crash/commands/"));

    //
    FS confFS = new FS();
    confFS.mount(loader, Path.get("/crash/"));

    //
    PluginContext context = new PluginContext(
        discovery,
        attributes,
        cmdFS,
        confFS,
        loader);

    //
    context.refresh();

    //
    start(context);
  }

  public void destroy() throws Exception {
    stop();
  }
}
