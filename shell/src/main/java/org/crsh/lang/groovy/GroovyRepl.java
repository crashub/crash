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
package org.crsh.lang.groovy;

import org.crsh.cli.impl.completion.CompletionMatch;
import org.crsh.plugin.CRaSHPlugin;
import org.crsh.repl.EvalResponse;
import org.crsh.repl.REPL;
import org.crsh.repl.REPLSession;

import java.util.logging.Logger;

/**
 * @author Julien Viet
 */
public class GroovyRepl extends CRaSHPlugin<REPL> implements REPL {

  /** . */
  static final Logger log = Logger.getLogger(GroovyRepl.class.getName());

  /** . */
  private static final REPL groovyRepl = getREPL();

  public static REPL getREPL() {
    try {
      Class<REPL> groovyReplClass = (Class<REPL>)GroovyRepl.class.getClassLoader().loadClass("org.crsh.lang.groovy.GroovyReplImpl");
      return groovyReplClass.newInstance();
    }
    catch (Exception e) {
      log.info("Plugin is inactive");
      return null;
    }
    catch (NoClassDefFoundError e) {
      log.info("Plugin is inactive");
      return null;
    }
  }

  @Override
  public REPL getImplementation() {
    return this;
  }

  @Override
  public boolean isActive() {
    return groovyRepl != null;
  }

  @Override
  public String getName() {
    return "groovy";
  }

  @Override
  public String getDescription() {
    return "The Groovy REPL provides a Groovy interpreter able to interact with shell commands";
  }

  @Override
  public EvalResponse eval(REPLSession session, String request) {
    if (groovyRepl != null) {
      return groovyRepl.eval(session, request);
    } else {
      throw new IllegalStateException("Groovy REPL is not available");
    }
  }

  @Override
  public CompletionMatch complete(REPLSession session, String prefix) {
    if (groovyRepl != null) {
      return groovyRepl.complete(session, prefix);
    } else {
      throw new IllegalStateException("Groovy REPL is not available");
    }
  }
}
