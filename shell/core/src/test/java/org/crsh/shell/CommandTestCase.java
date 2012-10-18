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

package org.crsh.shell;

public class CommandTestCase extends AbstractCommandTestCase {

  /** . */
  private final String no_ret = "class no_ret extends org.crsh.command.CRaSHCommand {\n" +
      "@Command\n" +
      "public void main() {\n" +
      "}\n" +
      "}";


  public void testUnknownCommand() throws Exception {
    assertUnknownCommand("bilto");
  }

  public void testFailure() throws Exception {
    Throwable t = assertEvalError("fail");
//    assertEquals(Exception.class, t.getClass());
  }

  public void testInvalid() throws Exception {
    assertUnknownCommand("invalid");
//    assertEquals(MultipleCompilationErrorsException.class, t.getClass());
  }

  public void testSimple() throws Exception {
    assertEquals("foo", assertOk("echo foo"));
  }

  public void testSession() throws Exception {
    assertEquals("null", assertOk("attribute foo"));
    lifeCycle.setAttribute("foo", "bar");
    assertEquals("bar", assertOk("attribute foo"));
    lifeCycle.setAttribute("foo", null);
    assertEquals("null", assertOk("attribute foo"));
  }

  public void testInvokeNoRet() throws Exception {
    String foo = "class foo extends org.crsh.command.CRaSHCommand {\n" +
      "@Command\n" +
      "public String main() {\n" +
      "no_ret()\n" +
      "}\n" +
      "}";
    lifeCycle.setCommand("foo", foo);
    lifeCycle.setCommand("no_ret", no_ret);

    //
    assertEquals("", assertOk("foo"));
  }

  public void testInvokeNoRetInScript() throws Exception {
    String foo = "no_ret()\n";
    lifeCycle.setCommand("foo", foo);
    lifeCycle.setCommand("no_ret", no_ret);

    //
    assertEquals("", assertOk("foo"));
  }

  public void testInvoke() throws Exception {
    String foo = "class foo extends org.crsh.command.CRaSHCommand {\n" +
      "@Command\n" +
      "public String main() {\n" +
      "echo 'bar'\n" +
      "}\n" +
      "}";
    lifeCycle.setCommand("foo", foo);

    //
    assertEquals("bar", assertOk("foo"));
  }

  public void testInvokeInScript() throws Exception {
    String foo = "echo 'bar'\n";
    lifeCycle.setCommand("foo", foo);

    //
    assertEquals("bar", assertOk("foo"));
  }

  public void testClosure() {
    String foo = "class foo extends org.crsh.command.CRaSHCommand {\n" +
      "@Command\n" +
      "public void main() {\n" +
      "def closure = echo\n" +
      "closure 'bar'\n" +
      "}\n" +
      "}";
    lifeCycle.setCommand("foo", foo);

    //
    assertEquals("bar", assertOk("foo"));
  }

  public void testFlush() {
    assertEquals("foobar", assertOk("echo -f 1 foo bar"));
    assertEquals("bar", evalOk("out << 'bar'; out.flush();"));
  }

  public void testResolveOut() {
    String resolve = "class resolve extends org.crsh.command.CRaSHCommand {\n" +
        "@Command\n" +
        "public org.crsh.command.PipeCommand<Object> main() {\n" +
        "return new org.crsh.command.PipeCommand<Object>() {\n" +
        "public void open() {\n" +
        "out << 'HELLO'\n" +
        "}\n" +
        "}\n" +
        "}\n" +
        "}";
    lifeCycle.setCommand("resolve", resolve);

    //
    assertEquals("HELLO", assertOk("resolve"));
  }
}
