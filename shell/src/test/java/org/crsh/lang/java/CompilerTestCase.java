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
package org.crsh.lang.java;

import org.crsh.AbstractTestCase;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Callable;

/** @author Julien Viet */
public class CompilerTestCase extends AbstractTestCase {

  public void testCompile() throws Exception {
    Compiler compiler = new Compiler();
    List<JavaClassFileObject> files = compiler.compile("A",
        "public class A implements java.util.concurrent.Callable<String> {\n" +
        "public String call() {\n" +
        "return \"hello\";\n" +
        "}\n" +
        "}");
    assertEquals(1, files.size());
    LoadingClassLoader loader = new LoadingClassLoader(Thread.currentThread().getContextClassLoader(), files);
    Class<?> A = loader.findClass("A");
    Callable<String> asCallable = (Callable<String>)A.newInstance();
    String ret = asCallable.call();
    assertEquals("hello", ret);
  }

  public void testImport() throws Exception {

    Compiler compiler = new Compiler();
    List<JavaClassFileObject> files = compiler.compile("foo.A", "package foo;\n public class A {}");
    assertEquals(1, files.size());
    final JavaClassFileObject aFile = files.get(0);
    JavaArchive a = ShrinkWrap.create(JavaArchive.class, "crash.jar");
    a.add(new ByteArrayAsset(files.get(0).getBytes()), "foo/A.class");
    a.setManifest(Thread.currentThread().getContextClassLoader().getResource("META-INF/MANIFEST.MF"));

    for (int i = 0;i < 2;i++) {

      //

      //
      ClassLoader cl;
      if (i == 0) {
        File tmp = File.createTempFile("crash", ".jar");
        assertTrue(tmp.delete());
        a.as(ZipExporter.class).exportTo(tmp);
        cl = new URLClassLoader(new URL[]{tmp.toURI().toURL()});
      } else {
        WebArchive b = ShrinkWrap.create(WebArchive.class);
        b.setManifest(Thread.currentThread().getContextClassLoader().getResource("META-INF/MANIFEST.MF"));
        b.addAsLibrary(a);
        final File tmp = File.createTempFile("crash", ".war");
        assertTrue(tmp.delete());
        b.as(ZipExporter.class).exportTo(tmp);
        cl = new ClassLoader(Thread.currentThread().getContextClassLoader()) {
          Class<?> aClass = null;
          @Override
          protected Class<?> findClass(String name) throws ClassNotFoundException {
            if (name.equals("foo.A")) {
              if (aClass == null) {
                // Normally we should use the bytes from the nested crash.jar but it's too difficult
                // so we just use what we compiled before as it is the same bytecode and already
                // available here
                byte[] bytes = aFile.getBytes();
                aClass = defineClass(name, bytes, 0, bytes.length);
              }
              return aClass;
            } else {
              return super.loadClass(name);
            }
          }
          @Override
          protected Enumeration<URL> findResources(String name) throws IOException {
            if ("META-INF/MANIFEST.MF".equals(name)) {
              URL u1 = new URL("jar:" + tmp.toURI().toURL() + "!/META-INF/MANIFEST.MF");
              URL u2 = new URL("jar:" + ("jar:" + tmp.toURI().toURL() + "!/WEB-INF/lib/crash.jar") + "!/META-INF/MANIFEST.MF");
              return Collections.enumeration(Arrays.asList(u1, u2));
            } else if ("foo".equals(name)) {
              String u = "jar:" + ("jar:" + tmp.toURI().toURL() + "!/WEB-INF/lib/crash.jar") + "!/foo/";
              return Collections.enumeration(Collections.singleton(new URL(u)));
            } else {
              return super.findResources(name);
            }
          }
        };
      }

      //
      compiler = new Compiler(cl);
      files = compiler.compile("B",
          "import foo.A;\n" +
              "public class B implements java.util.concurrent.Callable<A> {\n" +
              "public A call() {\n" +
              "return new A();\n" +
              "}\n" +
              "}");
      assertEquals(1, files.size());
      LoadingClassLoader loader = new LoadingClassLoader(cl, files);
      Class<?> B = loader.findClass("B");
      Callable<?> asCallable = (Callable<?>)B.newInstance();
      Object ret = asCallable.call();
      assertNotNull(ret);
      Class<?> A = ret.getClass();
      assertEquals("foo.A", A.getName());
      assertEquals(cl, A.getClassLoader());
    }

  }

  public void testCompilationFailure() throws Exception {
    Compiler compiler = new Compiler();
    try {
      compiler.compile("foo.A", "package foo;\n public class A {");
    }
    catch (CompilationFailureException e) {
      List<Diagnostic<? extends JavaFileObject>> errors = e.getErrors();
      assertEquals(1, errors.size());
      Diagnostic<? extends JavaFileObject> error = errors.get(0);
      assertEndsWith("/foo/A.java", error.getSource().getName());
    }
  }
}
