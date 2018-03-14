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
package org.crsh.console;

import jline.console.Operation;

/**
 * Tests that need to be ported.
 *
 * @author Julien Viet
 */
public class ConsoleViMoveTestCase extends AbstractConsoleTestCase {

  // Yank left
  public void testMoveLeft8() throws Exception {
/*
        console.setKeyMap(KeyMap.VI_INSERT);
        b = (new Buffer("word"))
            .escape()
            .append("3y")
            .append(left)
            .append("p")
            .enter();
        assertLine("wordwor", b, true);
*/
  }

  // Yank move right
  public void testMoveRight6() throws Exception {
    // Not implemented for the moment
/*
        console.setKeyMap(KeyMap.VI_INSERT);
        b = (new Buffer("a bunch of words"))
            .escape()
            .append("010y")
            .append(right)
            .append("$p")
            .enter();
        assertLine("a bunch of wordsa bunch of", b, true);
*/
  }

  // Yank to EOL
  public void testEndOfLine4() throws Exception {
    console.toInsert();
    console.init();
    console.on(KeyStrokes.of("chicken sushimi"));
    console.on(Operation.VI_MOVEMENT_MODE);
    console.on(Operation.VI_BEGINNING_OF_LINE_OR_ARG_DIGIT);
    console.on(KeyStrokes.RIGHT);
    console.on(KeyStrokes.RIGHT);
    console.on(Operation.VI_YANK_TO);
    console.on(Operation.END_OF_LINE);
    console.on(Operation.END_OF_LINE);
    console.on(Operation.VI_PUT);
//    console.on(KeyEvent.of("opsticks"));
//    assertEquals("chopsticks", getCurrentLine());
  }

  public void testWordRight6() {
/*
        b = (new Buffer("big brown pickles"))
            .escape()
            .append("02yw$p")
            .enter();
        assertLine("big brown picklesbig brown ", b, false);
*/
  }

  public void test_yy1() throws Exception {
//        /*
//         * This tests "yy" or yank-to + yank-to, which should yank the whole line
//         */
//    console.setKeyMap(KeyMap.VI_INSERT);
//    Buffer b = (new Buffer("abcdef"))
//        .escape()
//        .append("yyp")
//        .enter();
//    assertLine("abcdefabcdef", b, true);
  }

  public void testMoveModeAfterAcceptInMultiline() {
    console.init();
    console.toInsert();
    console.on(KeyStrokes.a);
    console.on(KeyStrokes.BACKSLASH);
    console.toMove();
    console.on(Operation.VI_MOVE_ACCEPT_LINE);
    assertEquals(Mode.VI_INSERT, console.getMode());
 }

}