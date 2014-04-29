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
package org.crsh.console.operations;

import jline.console.Operation;
import org.crsh.console.AbstractConsoleTestCase;

/**
 * @author Julien Viet
 */
public class UnmappedTestCase extends AbstractConsoleTestCase {

  public void testEmacs() {
    console.init();
    console.on(Operation.DO_LOWERCASE_VERSION);
    console.on(Operation.QUOTED_INSERT);
    console.on(Operation.REVERSE_SEARCH_HISTORY);
    console.on(Operation.FORWARD_SEARCH_HISTORY);
    console.on(Operation.YANK);
    console.on(Operation.CHARACTER_SEARCH);
    console.on(Operation.UNDO);
    console.on(Operation.RE_READ_INIT_FILE);
    console.on(Operation.START_KBD_MACRO);
    console.on(Operation.END_KBD_MACRO);
    console.on(Operation.CALL_LAST_KBD_MACRO);
    console.on(Operation.EXCHANGE_POINT_AND_MARK);
    console.on(Operation.TAB_INSERT);
    console.on(Operation.REVERT_LINE);
    console.on(Operation.YANK_NTH_ARG);
    console.on(Operation.CHARACTER_SEARCH_BACKWARD);
    console.on(Operation.SET_MARK);
    console.on(Operation.TILDE_EXPAND);
    console.on(Operation.INSERT_COMPLETIONS);
    console.on(Operation.DIGIT_ARGUMENT);
    console.on(Operation.YANK_LAST_ARG);
    console.on(Operation.POSSIBLE_COMPLETIONS);
    console.on(Operation.DELETE_HORIZONTAL_SPACE);
    console.on(Operation.NON_INCREMENTAL_REVERSE_SEARCH_HISTORY);

    // Doable
    console.on(Operation.CAPITALIZE_WORD);
    console.on(Operation.DOWNCASE_WORD);
    console.on(Operation.TRANSPOSE_WORDS);
    console.on(Operation.UPCASE_WORD);
    console.on(Operation.YANK_POP);
    console.on(Operation.TILDE_EXPAND);
  }

  public void testInsert() {
    console.init();
    console.toInsert();
    console.on(Operation.MENU_COMPLETE);
    console.on(Operation.MENU_COMPLETE_BACKWARD);
    console.on(Operation.REVERSE_SEARCH_HISTORY);
    console.on(Operation.FORWARD_SEARCH_HISTORY);
    console.on(Operation.QUOTED_INSERT);
    console.on(Operation.YANK);
    console.on(Operation.UNDO);
  }

  public void testMove() {
    console.init();
    console.toMove();
    console.on(Operation.VI_SEARCH);
  }
}
