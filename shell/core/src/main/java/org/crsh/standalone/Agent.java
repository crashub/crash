/*
 * Copyright (C) 2011 eXo Platform SAS.
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

import org.crsh.cmdline.ClassDescriptor;
import org.crsh.cmdline.CommandFactory;
import org.crsh.cmdline.annotations.Argument;
import org.crsh.cmdline.annotations.Command;
import org.crsh.cmdline.annotations.Option;
import org.crsh.cmdline.matcher.CommandMatch;
import org.crsh.cmdline.matcher.InvocationContext;
import org.crsh.cmdline.matcher.Matcher;
import org.crsh.shell.impl.CRaSH;
import org.crsh.shell.impl.CRaSHSession;
import org.crsh.term.processor.Processor;
import org.crsh.term.BaseTerm;
import org.crsh.term.Term;
import org.crsh.term.spi.net.TermIOClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.lang.instrument.Instrumentation;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class Agent {

  /** . */
  private static Logger log = LoggerFactory.getLogger(Agent.class);

  public static void agentmain(final String agentArgs, Instrumentation inst) throws Exception {
    log.info("CRaSH agent loaded");

    //
    Thread t = new Thread() {
      @Override
      public void run() {
        try {
          ClassDescriptor<Agent> c = CommandFactory.create(Agent.class);
          Matcher<Agent> matcher = Matcher.createMatcher("main", c);
          CommandMatch<Agent, ?, ?> match = matcher.match(agentArgs);
          match.invoke(new InvocationContext(), new Agent());
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    };

    //
    t.start();
    log.info("Spanned CRaSH thread " + t.getId() + " for further processing");
  }

  @Command
  public void main(
    @Option(names={"j","jar"})
    List<String> jars,
    @Option(names={"p","path"})
    List<String> paths,
    @Argument(name = "port")
    Integer port) throws Exception {

    //
    Bootstrap bootstrap = new Bootstrap(Thread.currentThread().getContextClassLoader());

    //
    if (paths != null) {
      for (String path : paths) {
        File mount = new File(path);
        bootstrap.addToMounts(mount);
      }
    }

    //
    if (jars != null) {
      for (String jar : jars) {
        File jarFile = new File(jar);
        bootstrap.addToClassPath(jarFile);
      }
    }

    // Do bootstrap
    bootstrap.bootstrap();

    //
    try {
      final TermIOClient client = new TermIOClient(port);
      log.info("Callback back remote on port " + port);
      client.connect();

      // Do stuff
      Term term = new BaseTerm(client);
      CRaSH crash = new CRaSH(bootstrap.getContext());
      Processor processor = new Processor(term, crash.createSession());
      processor.addListener(new Closeable() {
        public void close() {
          client.close();
        }
      });

      // Run
      processor.run();
    }
    finally {
      bootstrap.shutdown();
    }
  }
}
