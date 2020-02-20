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
import jline.AnsiWindowsTerminal;
import jline.Terminal;
import jline.TerminalFactory;
import jline.console.ConsoleReader;
import jline.internal.Configuration;
import org.crsh.cli.Argument;
import org.crsh.cli.Command;
import org.crsh.cli.Named;
import org.crsh.cli.Option;
import org.crsh.cli.Usage;
import org.crsh.cli.descriptor.CommandDescriptor;
import org.crsh.cli.impl.Delimiter;
import org.crsh.cli.impl.descriptor.IntrospectionException;
import org.crsh.cli.impl.invocation.InvocationMatch;
import org.crsh.cli.impl.invocation.InvocationMatcher;
import org.crsh.cli.impl.lang.CommandFactory;
import org.crsh.cli.impl.lang.Instance;
import org.crsh.cli.impl.lang.Util;
import org.crsh.command.ShellSafety;
import org.crsh.command.ShellSafetyFactory;
import org.crsh.console.jline.JLineProcessor;
import org.crsh.plugin.ResourceManager;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellFactory;
import org.crsh.shell.impl.remoting.RemoteServer;
import org.crsh.util.CloseableList;
import org.crsh.util.InterruptHandler;
import org.crsh.util.Utils;
import org.crsh.vfs.FS;
import org.crsh.vfs.Path;
import org.crsh.vfs.Resource;
import org.crsh.vfs.spi.Mount;
import org.crsh.vfs.spi.file.FileMountFactory;
import org.crsh.vfs.spi.url.ClassPathMountFactory;
import org.fusesource.jansi.AnsiConsole;

import java.io.BufferedOutputStream;
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

@Named("crash")
public class CRaSH {

  /** . */
  private static Logger log = Logger.getLogger(CRaSH.class.getName());

  /** . */
  private final CommandDescriptor<Instance<CRaSH>> descriptor;

  public CRaSH() throws IntrospectionException {
    this.descriptor = CommandFactory.DEFAULT.create(CRaSH.class);
  }

  private void copyCmd(org.crsh.vfs.File src, File dst) throws IOException {
    if (src.hasChildren()) {
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
    if (!src.hasChildren()) {
      if (!dst.exists()) {
        Resource resource = ResourceManager.loadConf(src);
        if (resource != null) {
          log.info("Copied resource " + src.getPath().getValue() + " to " + dst.getCanonicalPath());
          Utils.copy(new ByteArrayInputStream(resource.getContent()), new FileOutputStream(dst));
        }
      }
    }
  }

  private String toString(FS.Builder builder) {
    StringBuilder sb = new StringBuilder();
    List<Mount<?>> mounts = builder.getMounts();
    for (int i = 0;i < mounts.size();i++) {
      Mount<?> mount = mounts.get(i);
      if (i > 0) {
        sb.append(';');
      }
      sb.append(mount.getValue());
    }
    return sb.toString();
  }

  private FS.Builder createBuilder() throws IOException {
    FileMountFactory fileDriver = new FileMountFactory(Utils.getCurrentDirectory());
    ClassPathMountFactory classpathDriver = new ClassPathMountFactory(Thread.currentThread().getContextClassLoader());
    return new FS.Builder().register("file", fileDriver).register("classpath", classpathDriver);
  }

  @Command
  public void main(
    @Option(names= {"non-interactive"})
    @Usage("non interactive mode, the JVM io will not be used")
    Boolean nonInteractive,
    @Option(names={"c","cmd"})
    @Usage("the command mounts")
    String cmd,
    @Option(names={"conf"})
    @Usage("the conf mounts")
    String conf,
    @Option(names={"p","property"})
    @Usage("set a property of the form a=b")
    List<String> properties,
    @Option(names = {"cmd-folder"})
    @Usage("a folder in which commands should be extracted")
    String cmdFolder,
    @Option(names = {"conf-folder"})
    @Usage("a folder in which configuration should be extracted")
    String confFolder,
    @Argument(name = "pid")
    @Usage("the optional list of JVM process id to attach to")
    List<Integer> pids) throws Exception {

    //
    boolean interactive = nonInteractive == null || !nonInteractive;

    //
    if (conf == null) {
      conf = "classpath:/crash/";
    }
    FS.Builder confBuilder = createBuilder().mount(conf);
    if (confFolder != null) {
      File dst = new File(confFolder);
      if (!dst.isDirectory()) {
        throw new Exception("Directory " + dst.getAbsolutePath() + " does not exist");
      }
      org.crsh.vfs.File f = confBuilder.build().get(Path.get("/"));
      log.info("Extracting conf resources to " + dst.getAbsolutePath());
      for (org.crsh.vfs.File child : f.children()) {
        if (!child.hasChildren()) {
          copyConf(child, new File(dst, child.getName()));
        }
      }
      confBuilder = createBuilder().mount("file", Path.get(dst));
    }

    //
    if (cmd == null) {
      cmd = "classpath:/crash/commands/";
    }
    FS.Builder cmdBuilder = createBuilder().mount(cmd);
    if (cmdFolder != null) {
      File dst = new File(cmdFolder);
      if (!dst.isDirectory()) {
        throw new Exception("Directory " + dst.getAbsolutePath() + " does not exist");
      }
      org.crsh.vfs.File f = cmdBuilder.build().get(Path.get("/"));
      log.info("Extracting command resources to " + dst.getAbsolutePath());
      copyCmd(f, dst);
      cmdBuilder = createBuilder().mount("file", Path.get(dst));
    }

    //
    log.log(Level.INFO, "conf mounts: " + confBuilder.toString());
    log.log(Level.INFO, "cmd mounts: " + cmdBuilder.toString());


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
          String fileName = file.getCanonicalPath();
          if(fileName.charAt(0) != '/' && fileName.charAt(1) == ':') {
            // On window, the value of Class-Path in Manifest file must in form: /C:/path/lib/abc.jar
            fileName = fileName.replace(File.separatorChar, '/');
            buffer.append("/").append(fileName);

          } else {
            buffer.append(file.getCanonicalPath());
          }
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

      // Path configuration
      sb.append("--cmd ");
      Delimiter.EMPTY.escape(toString(cmdBuilder), sb);
      sb.append(' ');
      sb.append("--conf ");
      Delimiter.EMPTY.escape(toString(confBuilder), sb);
      sb.append(' ');

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
      final Bootstrap bootstrap = new Bootstrap(
          Thread.currentThread().getContextClassLoader(),
          confBuilder.build(),
          cmdBuilder.build());

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
        ShellSafety shellSafety = ShellSafetyFactory.getCurrentThreadShellSafety();
        shellSafety.setStandAlone(true);
        shell = factory.create(null, null, shellSafety);
      } else {
        shell = null;
      }
      closeable = null;
    }

    //
    if (shell != null) {

      //
      final Terminal term = TerminalFactory.create();

      //
      Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() {
          try {
            term.restore();
          }
          catch (Exception ignore) {
          }
        }
      });

      //
      String encoding = Configuration.getEncoding();

      // Use AnsiConsole only if term doesn't support Ansi
      PrintStream out;
      PrintStream err;
      boolean ansi;
      if (term.isAnsiSupported()) {
        out = new PrintStream(new BufferedOutputStream(term.wrapOutIfNeeded(new FileOutputStream(FileDescriptor.out)), 16384), false, encoding);
        err = new PrintStream(new BufferedOutputStream(term.wrapOutIfNeeded(new FileOutputStream(FileDescriptor.err)), 16384), false, encoding);
        ansi = true;
      } else {
        out = AnsiConsole.out;
        err = AnsiConsole.err;
        ansi = false;
      }

      //
      FileInputStream in = new FileInputStream(FileDescriptor.in);
      ConsoleReader reader = new ConsoleReader(null, in, out, term);

      //
      final JLineProcessor processor = new JLineProcessor(ansi, shell, reader, out);

      //
      InterruptHandler interruptHandler = new InterruptHandler(new Runnable() {
        @Override
        public void run() {
          processor.interrupt();
        }
      });
      interruptHandler.install();

      //
      Thread thread = new Thread(processor);
      thread.setDaemon(true);
      thread.start();

      //
      try {
        processor.closed();
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
    InvocationMatcher<Instance<CRaSH>> matcher = main.descriptor.matcher();
    InvocationMatch<Instance<CRaSH>> match = matcher.parse(line.toString());
    match.invoke(Util.wrap(main));
  }
}
