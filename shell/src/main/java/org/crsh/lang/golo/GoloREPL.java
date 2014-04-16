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
package org.crsh.lang.golo;

import fr.insalyon.citi.golo.compiler.GoloClassLoader;
import fr.insalyon.citi.golo.compiler.GoloCompilationException;
import fr.insalyon.citi.golo.compiler.parser.TokenMgrError;
import gololang.EvaluationEnvironment;
import org.crsh.cli.impl.Delimiter;
import org.crsh.cli.impl.completion.CompletionMatch;
import org.crsh.cli.spi.Completion;
import org.crsh.plugin.CRaSHPlugin;
import org.crsh.plugin.PluginContext;
import org.crsh.repl.EvalResponse;
import org.crsh.repl.REPL;
import org.crsh.repl.REPLSession;
import org.crsh.shell.ShellResponse;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/** @author Julien Viet */
public class GoloREPL extends CRaSHPlugin<REPL> implements REPL {

  /** . */
  private static final String replScript =
      "module repl\n" +
      "function eval = |script| {\n" +
      "  let env = gololang.EvaluationEnvironment()\n" +
      "  let ret = env: run(script)\n" +
      "} \n";

  /** . */
  private GoloClassLoader replLoader;

  /** . */
  // private Class<?> replClass;

  public GoloREPL() {
  }

  @Override
  public boolean isActive() {
    return true;
  }

  @Override
  public String getDescription() {
    return "The golo repl";
  }

  @Override
  public REPL getImplementation() {
    return this;
  }

  @Override
  public void init() {
    PluginContext context = getContext();
    ClassLoader loader = context.getLoader();
    replLoader = new GoloClassLoader(loader);
    // replClass = replLoader.load("repl.golo", new ByteArrayInputStream(replScript.getBytes()));
  }

  public String getName() {
    return "golo";
  }

  public EvalResponse eval(REPLSession session, String request) {

    try {
//      Method eval = replClass.getMethod("eval", Object.class);
      ClassLoader old = Thread.currentThread().getContextClassLoader();
      try {
        Thread.currentThread().setContextClassLoader(replLoader);
//        eval.invoke(null, request);

        //
//        request =
//            "local function println = |obj| {\n" +
//                "gololang.Predefined.println(\">>> \" + obj)\n" +
//                "}\n" +
//            request;

        String mainWrapper = "function main = { \n" + request + "\n}";

//        run(request);
        EvaluationEnvironment environment = new EvaluationEnvironment();
        Class<?> code = (Class<?>) environment.anonymousModule(mainWrapper);
        Method main = code.getDeclaredMethod("main");
        main.invoke(code);
      }
      finally {
        Thread.currentThread().setContextClassLoader(old);
      }
      return new EvalResponse.Response(ShellResponse.ok());
    }
//    catch (InvocationTargetException e) {
//      return new EvalResponse.Response(ShellResponse.evalError("Could not evaluate request", e.getCause()));
//    }
    catch (GoloCompilationException e) {
      return new EvalResponse.Response(ShellResponse.evalError("Could not evaluate request", e));
    }
    catch (Exception e) {
      return new EvalResponse.Response(ShellResponse.internalError("Could not evaluate request", e));
    }
    catch (TokenMgrError error) {
      return new EvalResponse.Response(ShellResponse.internalError("Could not parse", error));
    }


  }



  // Forked from gololang.EvaluationEnvironment

  private final List<String> imports = new LinkedList<>();

//  private static String anonymousModuleName() {
//    return "module anonymous" + System.nanoTime();
//  }

  public CompletionMatch complete(REPLSession session, String prefix) {
    return new CompletionMatch(Delimiter.EMPTY, Completion.create());
  }

//  public Object run(String source) {
//    return loadAndRun(source, "$_code");
//  }

//  private Object loadAndRun(String source, String target, String... argumentNames) {
//    try {
//      Class<?> module = wrapAndLoad(source, argumentNames);
//      return module.getMethod(target).invoke(null);
//    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
//      throw new RuntimeException(e);
//    }
//  }

//  private Class<?> wrapAndLoad(String source, String... argumentNames) {
//    StringBuilder builder = new StringBuilder()
//        .append(anonymousModuleName())
//        .append("\n");
//    for (String importSymbol : imports) {
//      builder.append("import ").append(importSymbol).append("\n");
//    }
//    builder.append("\nfunction $_code = ");
//    if (argumentNames.length > 0) {
//      builder.append("| ");
//      final int lastIndex = argumentNames.length - 1;
//      for (int i = 0; i < argumentNames.length; i++) {
//        builder.append(argumentNames[i]);
//        if (i < lastIndex) {
//          builder.append(", ");
//        }
//      }
//      builder.append(" |");
//    }
//    builder
//        .append(" {\n")
//        .append(source)
//        .append("\n}\n\n")
//        .append("function $_code_ref = -> ^$_code\n\n");
//    return (Class<?>) asModule(builder.toString());
//  }

//  public Object asModule(String source) {
//    try (InputStream in = new ByteArrayInputStream(source.getBytes())) {
//      return replLoader.load(anonymousFilename(), in);
//    } catch (IOException e) {
//      throw new RuntimeException(e);
//    } catch (GoloCompilationException e) {
//      e.setSourceCode(source);
//      throw e;
//    }
//  }

//  private static String anonymousFilename() {
//    return "$Anonymous$_" + System.nanoTime() + ".golo";
//  }
}
