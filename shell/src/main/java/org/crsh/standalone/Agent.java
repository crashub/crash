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
package org.crsh.standalone;

import org.crsh.cli.Required;
import org.crsh.cli.Usage;
import org.crsh.cli.descriptor.CommandDescriptor;
import org.crsh.cli.Argument;
import org.crsh.cli.Command;
import org.crsh.cli.Option;
import org.crsh.cli.impl.lang.CommandFactory;
import org.crsh.cli.impl.invocation.InvocationMatch;
import org.crsh.cli.impl.invocation.InvocationMatcher;
import org.crsh.cli.impl.lang.Instance;
import org.crsh.cli.impl.lang.Util;
import org.crsh.command.ShellSafety;
import org.crsh.command.ShellSafetyFactory;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellFactory;
import org.crsh.shell.impl.remoting.RemoteClient;
import org.crsh.util.Utils;
import org.crsh.vfs.FS;
import org.crsh.vfs.spi.file.FileMountFactory;
import org.crsh.vfs.spi.url.ClassPathMountFactory;

import java.lang.instrument.Instrumentation;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Agent {

  /** . */
  private static Logger log = Logger.getLogger(Agent.class.getName());

  public static void agentmain(final String agentArgs, final Instrumentation inst) throws Exception {
    log.log(Level.INFO, "CRaSH agent loaded");

    //
    Thread t = new Thread() {
      @Override
      public void run() {
        try {
          CommandDescriptor<Instance<Agent>> c = CommandFactory.DEFAULT.create(Agent.class);
          InvocationMatcher<Instance<Agent>> matcher = c.matcher();
          InvocationMatch<Instance<Agent>> match = matcher.parse(agentArgs);
          match.invoke(Util.wrap(new Agent(inst)));
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    };

    //
    t.start();
    log.log(Level.INFO, "Spawned CRaSH thread " + t.getId() + " for further processing");
  }

  /** . */
  private final Instrumentation instrumentation;

  public Agent(Instrumentation instrumentation) {
    this.instrumentation = instrumentation;
  }

  @Command
  public void main(
      @Required
      @Option(names={"c","cmd"})
      @Usage("the command path")
      String cmd,
      @Required
      @Option(names={"conf"})
      @Usage("the conf path")
      String conf,
      @Option(names={"p","property"})
      @Usage("set a property of the form a=b")
      List<String> properties,
      @Argument(name = "port")
      Integer port) throws Exception {

    //
    FileMountFactory fileDriver = new FileMountFactory(Utils.getCurrentDirectory());
    ClassPathMountFactory classpathDriver = new ClassPathMountFactory(Thread.currentThread().getContextClassLoader());

    //
    FS cmdFS = new FS.Builder().register("file", fileDriver).register("classpath", classpathDriver).mount(cmd).build();
    FS confFS = new FS.Builder().register("file", fileDriver).register("classpath", classpathDriver).mount(conf).build();
    Bootstrap bootstrap = new Bootstrap(Thread.currentThread().getContextClassLoader(), confFS, cmdFS);

    //
    if (properties != null) {
      Properties config = new Properties();
      for (String property : properties) {
        int index = property.indexOf('=');
        if (index == -1) {
          config.setProperty(property, "");
        } else {
          config.setProperty(property.substring(0, index), property.substring(index + 1));
        }
      }
      bootstrap.setConfig(config);
    }

    // Set the instrumentation available as an attribute
    Map<String, Object> attributes = Collections.<String, Object>singletonMap("instrumentation", instrumentation);
    bootstrap.setAttributes(attributes);

    // Do bootstrap
    bootstrap.bootstrap();

    //
    if (port != null) {
      try {
        ShellFactory factory = bootstrap.getContext().getPlugin(ShellFactory.class);
        Shell shell = factory.create(null,null, ShellSafetyFactory.getCurrentThreadShellSafety());
        RemoteClient client = new RemoteClient(port, shell);
        log.log(Level.INFO, "Callback back remote on port " + port);
        client.connect();
        client.getRunnable().run();
      }
      finally {
        bootstrap.shutdown();
      }
    }
  }
}
