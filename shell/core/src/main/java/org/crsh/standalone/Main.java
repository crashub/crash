/*
 * Copyright (C) 2010 eXo Platform SAS.
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

import com.sun.tools.attach.VirtualMachine;
import org.crsh.cmdline.ClassDescriptor;
import org.crsh.cmdline.CommandFactory;
import org.crsh.cmdline.IntrospectionException;
import org.crsh.cmdline.annotations.Argument;
import org.crsh.cmdline.annotations.Command;
import org.crsh.cmdline.annotations.Option;
import org.crsh.cmdline.annotations.Usage;
import org.crsh.cmdline.matcher.CommandMatch;
import org.crsh.cmdline.matcher.InvocationContext;
import org.crsh.cmdline.matcher.Matcher;
import org.crsh.shell.impl.CRaSH;
import org.crsh.term.processor.Processor;
import org.crsh.term.BaseTerm;
import org.crsh.term.Term;
import org.crsh.term.spi.jline.JLineIO;
import org.crsh.term.spi.net.TermIOServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.*;
import java.util.List;
import java.util.Properties;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Main {

  /** . */
  private static Logger log = LoggerFactory.getLogger(Main.class);

  /** . */
  private final ClassDescriptor<Main> descriptor;

  public Main() throws IntrospectionException {
    this.descriptor = CommandFactory.create(Main.class);
  }

  @Command
  public void main(
    @Option(names = {"h","help"})
    @Usage("display standalone mode help")
    Boolean help,
    @Option(names={"j","jar"})
    @Usage("specify a file system path of a jar added to the class path")
    List<String> jars,
    @Option(names={"m","mount"})
    @Usage("specify a file system path of a dir added to the mount path")
    List<String> mounts,
    @Option(names={"c","config"})
    @Usage("specify a config property of the form a=b")
    List<String> configEntries,
    @Argument(name = "pid")
    @Usage("the optional JVM process id to attach to")
    Integer pid) throws Exception {

    //
    if (Boolean.TRUE.equals(help)) {
      descriptor.printUsage(System.out);
    } else if (pid != null) {

      // Standalone
      URL url = Main.class.getProtectionDomain().getCodeSource().getLocation();
      java.io.File f = new java.io.File(url.toURI());
      log.info("Attaching to remote process " + pid);
      VirtualMachine vm = VirtualMachine.attach("" + pid);

      //
      TermIOServer server = new TermIOServer(new JLineIO(), 0);
      int port = server.bind();
      log.info("Callback server set on port " + port);

      // Build the options
      StringBuilder sb = new StringBuilder();

      // Rewrite absolute path
      if (mounts != null) {
        for (String mounth : mounts) {
          File fileMount = new File(mounth);
          if (fileMount.exists()) {
            sb.append("--mount ").append(fileMount.getCanonicalPath()).append(' ');
          }
        }
      }

      // Rewrite canonical path
      if (jars != null) {
        for (String jar : jars) {
          File file = new File(jar);
          if (file.exists()) {
            sb.append("--jar ").append(file.getCanonicalPath()).append(' ');
          }
        }
      }

      // Propagate canonical config
      if (configEntries != null) {
        for (String config : configEntries) {
          sb.append("--config" ).append(config).append(' ');
        }
      }

      // Append callback port
      sb.append(port);

      //
      String options = sb.toString();
      log.info("Loading agent with command " + options);
      vm.loadAgent(f.getCanonicalPath(), options);

      //
      try {
        server.accept();
        while (server.execute()) {
          //
        }
      } finally {
        vm.detach();
      }
    } else {
      final Bootstrap bootstrap = new Bootstrap(Thread.currentThread().getContextClassLoader());

      //
      if (mounts != null) {
        for (String mount : mounts) {
          File mountFile = new File(mount);
          bootstrap.addToMounts(mountFile);
        }
      }

      //
      if (jars != null) {
        for (String jar : jars) {
          File jarFile = new File(jar);
          bootstrap.addToClassPath(jarFile);
        }
      }

      //
      if (configEntries != null) {
        Properties config = new Properties();
        for (String configEntry : configEntries) {
          int index = configEntry.indexOf('=');
          if (index == -1) {
            config.setProperty(configEntry, "");
          } else {
            config.setProperty(configEntry.substring(0, index), configEntry.substring(index + 1));
          }
        }
        bootstrap.setConfig(config);
      }

      // Register shutdown hook
      Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() {
          // Should trigger some kind of run interruption
        }
      });

      // Do bootstrap
      bootstrap.bootstrap();

      // Start crash for this command line
      Term term = new BaseTerm(new JLineIO());
      CRaSH crash = new CRaSH(bootstrap.getContext());
      Processor processor = new Processor(term, crash.createSession());

      //
      try {
        processor.run();
      }
      finally {
        bootstrap.shutdown();
      }
    }
  }

  public static void main(String[] args) throws Exception {

    StringBuilder line = new StringBuilder();
    for (int i = 0;i < args.length;i++) {
      if (i  > 0) {
        line.append(' ');
      }
      line.append(args[i]);
    }

    //
    Main main = new Main();
    Matcher<Main> matcher = Matcher.createMatcher("main", main.descriptor);
    CommandMatch<Main, ?, ?> match = matcher.match(line.toString());
    match.invoke(new InvocationContext(), new Main());
  }
}
