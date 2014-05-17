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
package org.crsh.lang.impl.java;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** @author Julien Viet */
class Compiler {

  /** . */
  private final ClassLoader classLoader;

  Compiler() {
    this.classLoader = Thread.currentThread().getContextClassLoader();
  }

  Compiler(ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  List<JavaClassFileObject> compile(String className, String source) throws IOException, CompilationFailureException {

    // Get compiler
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

    // Diagnostics
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

    // The compiler
    JavaFileManagerImpl fileManager = new JavaFileManagerImpl(
        compiler.getStandardFileManager(diagnostics, null, Charset.defaultCharset()),
        new ClasspathResolver(classLoader));

    //
    List<JavaFileObject> sources;
    try {
      sources = Collections.<JavaFileObject>singletonList(new JavaSourceFileObject(className, source));
    }
    catch (URISyntaxException e) {
      throw new IOException(e);
    }

    // Compile
    JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null, sources);

    //
    Boolean ok = task.call();
    if (!ok) {
      ArrayList<Diagnostic<? extends JavaFileObject>> errors = new ArrayList<Diagnostic<? extends JavaFileObject>>();
      for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
        if (diagnostic.getKind() == Diagnostic.Kind.ERROR) {
          errors.add(diagnostic);
        }
      }
      throw new CompilationFailureException(errors);
    } else {
      return new ArrayList<JavaClassFileObject>(fileManager.getClasses());
    }
  }

}
