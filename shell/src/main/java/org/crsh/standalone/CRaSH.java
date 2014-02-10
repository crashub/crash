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

import com.sun.tools.attach.VirtualMachine;
import jline.NoInterruptUnixTerminal;
import org.crsh.cli.descriptor.CommandDescriptor;
import jline.Terminal;
import jline.TerminalFactory;
import org.crsh.cli.impl.Delimiter;
import org.crsh.cli.impl.descriptor.IntrospectionException;
import org.crsh.cli.Argument;
import org.crsh.cli.Command;
import org.crsh.cli.Option;
import org.crsh.cli.Usage;
import org.crsh.cli.impl.lang.CommandFactory;
import org.crsh.cli.impl.invocation.InvocationMatch;
import org.crsh.cli.impl.invocation.InvocationMatcher;
import org.crsh.plugin.ResourceManager;
import org.crsh.processor.jline.JLineProcessor;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellFactory;
import org.crsh.shell.impl.remoting.RemoteServer;
import org.crsh.util.CloseableList;
import org.crsh.util.Utils;
import org.crsh.vfs.FS;
import org.crsh.vfs.Path;
import org.crsh.vfs.Resource;
import org.fusesource.jansi.AnsiConsole;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class CRaSH {

  /** . */
  private static Logger log = Logger.getLogger(CRaSH.class.getName());

  /** . */
  private final CommandDescriptor<CRaSH> descriptor;

  public CRaSH() throws IntrospectionException {
    this.descriptor = CommandFactory.DEFAULT.create(CRaSH.class);
  }

  private void copyCmd(org.crsh.vfs.File src, File dst) throws IOException {
    if (src.isDir()) {
      if (!dst.exists()) {
        if (dst.mkdir()) {
          log.fine("Could not create dir " + dst.getCanonicalPath());
        }
      }
      if (dst.exists() && dst.isDirectory()) {
        for (org.crsh.vfs.File child : src.children()) {
          copyCmd(child, new File(dst, child.getName()));
        }
      }
    } else {
      if (!dst.exists()) {
        Resource resource = src.getResource();
        if (resource != null) {
          log.info("Copied command " + src.getPath().getValue() + " to " + dst.getCanonicalPath());
          Utils.copy(new ByteArrayInputStream(resource.getContent()), new FileOutputStream(dst));
        }
      }
    }
  }

  private void copyConf(org.crsh.vfs.File src, File dst) throws IOException {
    if (!src.isDir()) {
      if (!dst.exists()) {
        Resource resource = ResourceManager.loadConf(src);
        if (resource != null) {
          log.info("Copied resource " + src.getPath().getValue() + " to " + dst.getCanonicalPath());
          Utils.copy(new ByteArrayInputStream(resource.getContent()), new FileOutputStream(dst));
        }
      }
    }
  }

  @Command
  public void main(
    @Option(names= {"non-interactive"})
    @Usage("non interactive mode, the JVM io will not be used")
    Boolean nonInteractive,
    @Option(names={"c","cmd"})
    @Usage("adds a dir to the command path")
    List<String> cmds,
    @Option(names={"conf"})
    @Usage("adds a dir to the conf path")
    List<String> confs,
    @Option(names={"p","property"})
    @Usage("set a property of the form a=b")
    List<String> properties,
    @Option(names = {"cmd-mode"})
    @Usage("the cmd mode (read or copy), copy mode requires at least one cmd path to be specified")
    ResourceMode cmdMode,
    @Option(names = {"conf-mode"})
    @Usage("the conf mode (read of copy), copy mode requires at least one conf path to be specified")
    ResourceMode confMode,
    @Argument(name = "pid")
    @Usage("the optional list of JVM process id to attach to")
    List<Integer> pids) throws Exception {

    //
    boolean copyCmd = cmdMode != ResourceMode.read && cmds != null && cmds.size() > 0;
    boolean copyConf = confMode != ResourceMode.read && confs != null && confs.size() > 0;
    boolean interactive = nonInteractive == null || !nonInteractive;

    //
    if (copyCmd) {
      File dst = new File(cmds.get(0));
      if (!dst.isDirectory()) {
        throw new Exception("Directory " + dst.getAbsolutePath() + " does not exist");
      }
      FS fs = new FS();
      fs.mount(Thread.currentThread().getContextClassLoader(), Path.get("/crash/commands/"));
      org.crsh.vfs.File f = fs.get(Path.get("/"));
      log.info("Copying command classpath resources");
      copyCmd(f, dst);
    }

    //
    if (copyConf) {
      File dst = new File(confs.get(0));
      if (!dst.isDirectory()) {
        throw new Exception("Directory " + dst.getAbsolutePath() + " does not exist");
      }
      FS fs = new FS();
      fs.mount(Thread.currentThread().getContextClassLoader(), Path.get("/crash/"));
      org.crsh.vfs.File f = fs.get(Path.get("/"));
      log.info("Copying conf classpath resources");
      for (org.crsh.vfs.File child : f.children()) {
        if (!child.isDir()) {
          copyConf(child, new File(dst, child.getName()));
        }
      }
    }

    //
    CloseableList closeable = new CloseableList();
    Shell shell;
    if (pids != null && pids.size() > 0) {

      //
      if (interactive && pids.size() > 1) {
        throw new Exception("Cannot attach to more than one JVM in interactive mode");
      }

      // Compute classpath
      String classpath = System.getProperty("java.class.path");
      String sep = System.getProperty("path.separator");
      StringBuilder buffer = new StringBuilder();
      for (String path : classpath.split(Pattern.quote(sep))) {
        File file = new File(path);
        if (file.exists()) {
          if (buffer.length() > 0) {
            buffer.append(' ');
          }
          buffer.append(file.getCanonicalPath());
        }
      }

      // Create manifest
      Manifest manifest = new Manifest();
      Attributes attributes = manifest.getMainAttributes();
      attributes.putValue("Agent-Class", Agent.class.getName());
      attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
      attributes.put(Attributes.Name.CLASS_PATH, buffer.toString());

      // Create jar file
      File agentFile = File.createTempFile("agent", ".jar");
      agentFile.deleteOnExit();
      JarOutputStream out = new JarOutputStream(new FileOutputStream(agentFile), manifest);
      out.close();
      log.log(Level.INFO, "Created agent jar " + agentFile.getCanonicalPath());

      // Build the options
      StringBuilder sb = new StringBuilder();

      // Rewrite canonical path
      if (copyCmd) {
        sb.append("--cmd-mode copy ");
      } else {
        sb.append("--cmd-mode read ");
      }
      if (cmds != null) {
        for (String cmd : cmds) {
          File cmdPath = new File(cmd);
          if (cmdPath.exists()) {
            sb.append("--cmd ");
            Delimiter.EMPTY.escape(cmdPath.getCanonicalPath(), sb);
            sb.append(' ');
          }
        }
      }

      // Rewrite canonical path
      if (copyCmd) {
        sb.append("--conf-mode copy ");
      } else {
        sb.append("--conf-mode read ");
      }
      if (confs != null) {
        for (String conf : confs) {
          File confPath = new File(conf);
          if (confPath.exists()) {
            sb.append("--conf ");
            Delimiter.EMPTY.escape(confPath.getCanonicalPath(), sb);
            sb.append(' ');
          }
        }
      }

      // Propagate canonical config
      if (properties != null) {
        for (String property : properties) {
          sb.append("--property ");
          Delimiter.EMPTY.escape(property, sb);
          sb.append(' ');
        }
      }

      //
      if (interactive) {
        RemoteServer server = new RemoteServer(0);
        int port = server.bind();
        log.log(Level.INFO, "Callback server set on port " + port);
        sb.append(port);
        String options = sb.toString();
        Integer pid = pids.get(0);
        final VirtualMachine vm = VirtualMachine.attach("" + pid);
        log.log(Level.INFO, "Loading agent with command " + options + " as agent " + agentFile.getCanonicalPath());
        vm.loadAgent(agentFile.getCanonicalPath(), options);
        server.accept();
        shell = server.getShell();
        closeable.add(new Closeable() {
          public void close() throws IOException {
            vm.detach();
          }
        });
      } else {
        for (Integer pid : pids) {
          log.log(Level.INFO, "Attaching to remote process " + pid);
          VirtualMachine vm = VirtualMachine.attach("" + pid);
          String options = sb.toString();
          log.log(Level.INFO, "Loading agent with command " + options + " as agent " + agentFile.getCanonicalPath());
          vm.loadAgent(agentFile.getCanonicalPath(), options);
        }
        shell = null;
      }
    } else {
      final Bootstrap bootstrap = new Bootstrap(Thread.currentThread().getContextClassLoader());

      //
      if (!copyCmd) {
        bootstrap.addToCmdPath(Path.get("/crash/commands/"));
      }
      if (cmds != null) {
        for (String cmd : cmds) {
          File cmdPath = new File(cmd);
          bootstrap.addToCmdPath(cmdPath);
        }
      }

      //
      if (!copyConf) {
        bootstrap.addToConfPath(Path.get("/crash/"));
      }
      if (confs != null) {
        for (String conf : confs) {
          File confPath = new File(conf);
          bootstrap.addToConfPath(confPath);
        }
      }

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

      // Register shutdown hook
      Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() {
          // Should trigger some kind of run interruption
        }
      });

      // Do bootstrap
      bootstrap.bootstrap();
      Runtime.getRuntime().addShutdownHook(new Thread(){
        @Override
        public void run() {
          bootstrap.shutdown();
        }
      });

      //
      if (interactive) {
        ShellFactory factory = bootstrap.getContext().getPlugin(ShellFactory.class);
        shell = factory.create(null);
      } else {
        shell = null;
      }
      closeable = null;
    }

    //
    if (shell != null) {

      // Start crash for this command line
      jline.TerminalFactory.registerFlavor(jline.TerminalFactory.Flavor.UNIX, NoInterruptUnixTerminal.class);
      final Terminal term = TerminalFactory.create();
      Runtime.getRuntime().addShutdownHook(new Thread(){
        @Override
        public void run() {
          try {
            term.restore();
          }
          catch (Exception ignore) {
          }
        }
      });

      // Use AnsiConsole only if term doesn't support Ansi
      PrintStream out = System.out;
      PrintStream err = System.err;
      if (!term.isAnsiSupported()) {
        out = AnsiConsole.out;
        err = AnsiConsole.err;
      }

      //
      FileInputStream in = new FileInputStream(FileDescriptor.in);
      final JLineProcessor processor = new JLineProcessor( shell, in, out, err, term);

      //
      try {
        processor.run();
      }
      catch (Throwable t) {
        t.printStackTrace();
      }
      finally {

        //
        if (closeable != null) {
          Utils.close(closeable);
        }

        // Force exit
        System.exit(0);
      }
    }
  }

  public static void main(String[] args) throws Exception {

    StringBuilder line = new StringBuilder();
    for (int i = 0;i < args.length;i++) {
      if (i  > 0) {
        line.append(' ');
      }
      Delimiter.EMPTY.escape(args[i], line);
    }

    //
    CRaSH main = new CRaSH();
    InvocationMatcher<CRaSH> matcher = main.descriptor.matcher("main");
    InvocationMatch<CRaSH> match = matcher.parse(line.toString());
    match.invoke(new CRaSH());
  }
}
