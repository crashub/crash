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

package org.crsh.lang.impl.groovy;

import groovy.lang.GroovyClassLoader;
import junit.framework.TestCase;
import org.crsh.cli.Argument;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ArgumentNameTestCase extends TestCase {

  public void testFoo() throws Exception {


    GroovyClassLoader gcl = new GroovyClassLoader();
    Class<?> clazz = gcl.parseClass("public class foo {\n" +
      "@Argument private String a;" +
      "@Argument(name=\"some\") private String b;" +
      "public void bar(" +
      "@Argument String c, " +
      "@Argument(name=\"some\") String d) {}\n" +
      "}");

    //
    Field a = clazz.getDeclaredField("a");
    Argument aArg = a.getAnnotation(Argument.class);
    assertEquals("a", aArg.name());

    //
    Field b = clazz.getDeclaredField("b");
    Argument bArg = b.getAnnotation(Argument.class);
    assertEquals("some", bArg.name());

    //
    Method m = clazz.getDeclaredMethod("bar", String.class, String.class);
    Annotation[][] mAnnotations = m.getParameterAnnotations();
    assertEquals("c", ((Argument)mAnnotations[0][0]).name());
    assertEquals("some", ((Argument)mAnnotations[1][0]).name());
  }
}
