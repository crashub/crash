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

package org.crsh.util;

import junit.framework.TestCase;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UtilsTestCase extends TestCase {
  
  public void testGlobexToRegex() throws Exception {

    // Plain
    assertMatchGlob("foo", "foo");
    assertNotMatchGlob("bar", "foo");

    // * Wildcard
    assertMatchGlob("foo", "*foo");
    assertMatchGlob("barfoo", "*foo");
    assertNotMatchGlob("bar", "*foo");
    assertMatchGlob("foo", "foo*");
    assertMatchGlob("foobar", "foo*");
    assertNotMatchGlob("bar", "foo*");
    assertMatchGlob("fo", "f*o");
    assertMatchGlob("foo", "f*o");
    assertMatchGlob("fbo", "f*o");
    assertNotMatchGlob("bar", "f*o");

    // ? Wildcard
    assertNotMatchGlob("foo", "?foo");
    assertMatchGlob("afoo", "?foo");
    assertNotMatchGlob("abfoo", "?foo");
    assertNotMatchGlob("foo", "foo?");
    assertMatchGlob("fooa", "foo?");
    assertNotMatchGlob("fooab", "foo?");
    assertNotMatchGlob("fo", "f?o");
    assertMatchGlob("foo", "f?o");
    assertMatchGlob("fao", "f?o");
    assertNotMatchGlob("fabo", "f?o");

    // Range
    assertMatchGlob("foo", "f[oa]o");
    assertMatchGlob("fao", "f[oa]o");
    assertNotMatchGlob("fo", "f[oa]o");
    assertNotMatchGlob("fooo", "f[oa]o");

    // Escape
    assertMatchGlob("*", "\\*");
    assertMatchGlob("?", "\\?");
    assertMatchGlob("[]", "\\[]");

    // Special cases
    assertMatchGlob("f*o", "f[*]o");
    assertMatchGlob("f?o", "f[?]o");
  }

  private void assertMatchGlob(String test, String globex) {
    Pattern p = Pattern.compile("^" + Utils.globexToRegex(globex) + "$");
    Matcher matcher = p.matcher(test);
    assertTrue("Was expecting " + test + " to match " + globex, matcher.matches());
  }

  private void assertNotMatchGlob(String test, String globex) {
    Pattern p = Pattern.compile("^" + Utils.globexToRegex(globex) + "$");
    Matcher matcher = p.matcher(test);
    assertFalse("Was expecting " + test + " to not match " + globex, matcher.matches());
  }

  public void testInterpolate() {
    Map<String,String> context = Collections.singletonMap("foo", "bar");
    assertEquals("", Utils.interpolate("", context));
    assertEquals("$", Utils.interpolate("$", context));
    assertEquals("${foo}", Utils.interpolate("\\${foo}", context));
    assertEquals("${", Utils.interpolate("${", context));
    assertEquals("${a", Utils.interpolate("${a", context));
    assertEquals("bar", Utils.interpolate("${foo}", context));
    assertEquals("<bar>", Utils.interpolate("<${foo}>", context));
    assertEquals("<bar></bar>", Utils.interpolate("<${foo}></${foo}>", context));
    assertEquals("", Utils.interpolate("${bar}", context));
    assertEquals("juu", Utils.interpolate("${bar:-juu}", context));
    assertEquals("bar", Utils.interpolate("${foo:-juu}", context));
    assertEquals("", Utils.interpolate("${bar:-}", context));
    assertEquals("juu", Utils.interpolate("${:-juu}", context));
    assertEquals(":-", Utils.interpolate("${bar:-:-}", context));
  }
}
