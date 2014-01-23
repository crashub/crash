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
 * The current status of the editor state machine. It decodes a command line operation according
 * to the current status and its possible state.
 *
 * @author Julien Viet
 */
public abstract class Status {

  public abstract String getKeyMap();

  public void on(Console console, Operation operation, int... buffer) {
    String message = "Operation " + operation + " not mapped in " + getClass().getSimpleName() + " mode";
    System.err.println(message);
  }

  public static class Emacs extends Status {

    @Override
    public final String getKeyMap() {
      return "emacs";
    }

    @Override
    public void on(Console console, Operation operation, int... buffer) {
      switch (operation) {
        case SELF_INSERT:
          console.on(new KeyEvent(operation, EditorAction.INSERT, buffer));
          break;
        case VI_EDITING_MODE:
          console.setMode(new Status.Insert());
          break;
        case BACKWARD_DELETE_CHAR:
          console.on(new KeyEvent(operation, EditorAction.DELETE_PREV_CHAR, buffer));
          break;
        case BACKWARD_CHAR:
          console.on(new KeyEvent(operation, EditorAction.LEFT, buffer));
          break;
        case FORWARD_CHAR:
          console.on(new KeyEvent(operation, EditorAction.RIGHT, buffer));
          break;
        case DELETE_CHAR:
          console.on(new KeyEvent(operation, EditorAction.DELETE_NEXT_CHAR, buffer));
          break;
        case BACKWARD_WORD:
          console.on(new KeyEvent(operation, EditorAction.MOVE_PREV_WORD_AT_BEGINNING, buffer));
          break;
        case FORWARD_WORD:
          console.on(new KeyEvent(operation, EditorAction.MOVE_NEXT_WORD_AFTER_END, buffer));
          break;
        case BEGINNING_OF_LINE:
          console.on(new KeyEvent(operation, EditorAction.MOVE_BEGINNING, buffer));
          break;
        case EXIT_OR_DELETE_CHAR:
          console.on(new KeyEvent(operation, EditorAction.EOF_MAYBE, buffer));
          break;
        case END_OF_LINE:
          console.on(new KeyEvent(operation, EditorAction.MOVE_END, buffer));
          break;
        case COMPLETE:
          console.on(new KeyEvent(operation, EditorAction.COMPLETE, buffer));
          break;
        case ACCEPT_LINE:
          console.on(new KeyEvent(operation, EditorAction.ENTER, buffer));
          break;
        case KILL_LINE:
          console.on(new KeyEvent(operation, EditorAction.DELETE_END, buffer));
          break;
        case PREVIOUS_HISTORY:
          console.on(new KeyEvent(operation, EditorAction.HISTORY_PREV, buffer));
          break;
        case NEXT_HISTORY:
          console.on(new KeyEvent(operation, EditorAction.HISTORY_NEXT, buffer));
          break;
        case TRANSPOSE_CHARS:
          console.on(new KeyEvent(operation, EditorAction.TRANSPOSE_CHARS, buffer));
          break;
        case UNIX_LINE_DISCARD:
          console.on(new KeyEvent(operation, EditorAction.DELETE_BEGINNING, buffer));
          break;
        case UNIX_WORD_RUBOUT:
          console.on(new KeyEvent(operation, EditorAction.DELETE_PREV_WORD, buffer));
          break;
        case BACKWARD_KILL_WORD:
          console.on(new KeyEvent(operation, EditorAction.DELETE_PREV_WORD, buffer));
          break;
        case INSERT_COMMENT:
          console.on(new KeyEvent(operation, EditorAction.INSERT_COMMENT, buffer));
          break;
        case BEGINNING_OF_HISTORY:
          console.on(new KeyEvent(operation, EditorAction.HISTORY_FIRST, buffer));
          break;
        case END_OF_HISTORY:
          console.on(new KeyEvent(operation, EditorAction.HISTORY_LAST, buffer));
          break;
        case INTERRUPT:
          console.on(new KeyEvent(operation, EditorAction.INTERRUPT, buffer));
          break;
        case CLEAR_SCREEN:
          console.on(new KeyEvent(operation, EditorAction.CLS, buffer));
          break;
        case DO_LOWERCASE_VERSION:
        case ABORT:
        case EXCHANGE_POINT_AND_MARK:
        case QUOTED_INSERT:
        case REVERSE_SEARCH_HISTORY:
        case FORWARD_SEARCH_HISTORY:
        case YANK:
        case CHARACTER_SEARCH:
        case UNDO:
        case RE_READ_INIT_FILE:
        case START_KBD_MACRO:
        case END_KBD_MACRO:
        case CALL_LAST_KBD_MACRO:
        case TAB_INSERT:
        case REVERT_LINE:
        case YANK_NTH_ARG:
        case CHARACTER_SEARCH_BACKWARD:
        case SET_MARK:
        case TILDE_EXPAND:
        case INSERT_COMPLETIONS:
        case DIGIT_ARGUMENT:
        case YANK_LAST_ARG:
        case POSSIBLE_COMPLETIONS:
        case DELETE_HORIZONTAL_SPACE:
        case CAPITALIZE_WORD:
        case DOWNCASE_WORD:
        case NON_INCREMENTAL_REVERSE_SEARCH_HISTORY:
        case TRANSPOSE_WORDS:
        case UPCASE_WORD:
        case YANK_POP:
          // Not yet implemented
          break;
        default:
          super.on(console, operation, buffer);
      }
    }
  }

  public static class Insert extends Status {

    @Override
    public final String getKeyMap() {
      return "vi-insert";
    }

    @Override
    public void on(Console console, Operation operation, int... buffer) {
      switch (operation) {
        case VI_MOVEMENT_MODE:
          console.setMode(new Status.Move());
          console.on(new KeyEvent(operation, EditorAction.LEFT, buffer));
          break;
        case FORWARD_CHAR:
          console.on(new KeyEvent(operation, EditorAction.RIGHT, buffer));
          break;
        case BACKWARD_CHAR:
          console.on(new KeyEvent(operation, EditorAction.LEFT, buffer));
          break;
        case VI_NEXT_WORD:
          console.on(new KeyEvent(operation, EditorAction.MOVE_NEXT_WORD_AT_BEGINNING, buffer));
          break;
        case VI_EOF_MAYBE:
          console.on(new KeyEvent(operation, EditorAction.EOF_MAYBE, buffer));
          break;
        case SELF_INSERT:
          console.on(new KeyEvent(operation, EditorAction.INSERT, buffer));
          break;
        case BACKWARD_DELETE_CHAR:
          console.on(new KeyEvent(operation, EditorAction.DELETE_PREV_CHAR, buffer));
          break;
        case COMPLETE:
          console.on(new KeyEvent(operation, EditorAction.COMPLETE, buffer));
          break;
        case ACCEPT_LINE:
          console.on(new KeyEvent(operation, EditorAction.ENTER, buffer));
          break;
        case TRANSPOSE_CHARS:
          console.on(new KeyEvent(operation, EditorAction.TRANSPOSE_CHARS, buffer));
          break;
        case UNIX_LINE_DISCARD:
          console.on(new KeyEvent(operation, EditorAction.DELETE_BEGINNING, buffer));
          break;
        case UNIX_WORD_RUBOUT:
          console.on(new KeyEvent(operation, EditorAction.DELETE_PREV_WORD, buffer));
          break;
        case INTERRUPT:
          console.on(new KeyEvent(operation, EditorAction.INTERRUPT, buffer));
          break;
        case PREVIOUS_HISTORY:
          console.on(new KeyEvent(operation, EditorAction.HISTORY_PREV, buffer));
          break;
        case NEXT_HISTORY:
          console.on(new KeyEvent(operation, EditorAction.HISTORY_NEXT, buffer));
          break;
        case BEGINNING_OF_HISTORY:
          console.on(new KeyEvent(operation, EditorAction.HISTORY_FIRST, buffer));
          break;
        case END_OF_HISTORY:
          console.on(new KeyEvent(operation, EditorAction.HISTORY_LAST, buffer));
          break;
        case MENU_COMPLETE:
        case MENU_COMPLETE_BACKWARD:
        case REVERSE_SEARCH_HISTORY:
        case FORWARD_SEARCH_HISTORY:
        case QUOTED_INSERT:
        case YANK:
        case UNDO:
          // Not yet implemented
          break;
        default:
          super.on(console, operation, buffer);
      }
    }
  }

  public static class Move extends Status {

    @Override
    public final String getKeyMap() {
      return "vi-move";
    }

    @Override
    public void on(Console console, Operation operation, int... buffer) {
      switch (operation) {
        case VI_MOVE_ACCEPT_LINE:
          console.on(new KeyEvent(operation, EditorAction.ENTER, buffer));
          break;
        case VI_INSERTION_MODE:
          console.setMode(new Status.Insert());
          break;
        case VI_INSERT_BEG:
          console.on(new KeyEvent(operation, EditorAction.MOVE_BEGINNING));
          console.setMode(new Status.Insert());
          break;
        case VI_INSERT_COMMENT:
          console.on(new KeyEvent(operation, EditorAction.INSERT_COMMENT, buffer));
          break;
        case BACKWARD_DELETE_CHAR:
          console.on(new KeyEvent(operation, EditorAction.DELETE_PREV_CHAR, buffer));
          break;
        case VI_DELETE:
          console.on(new KeyEvent(operation, EditorAction.DELETE_NEXT_CHAR, buffer));
          break;
        case KILL_LINE:
          console.on(new KeyEvent(operation, EditorAction.DELETE_END, buffer));
          break;
        case VI_DELETE_TO:
          if (buffer.length > 0 && buffer[0] == 'D') {
            // Workaround since it is only implemented in jline 2.12 with Operation.VI_DELETE_TO_EOL
            // see https://github.com/jline/jline2/commit/f60432ffbd8322f53abb2d284e1f92f94acf0cc8
            console.on(new KeyEvent(operation, EditorAction.DELETE_END, buffer));
          } else {
            console.setMode(new DeleteTo());
          }
          break;
        case VI_NEXT_WORD:
          console.on(new KeyEvent(operation, EditorAction.MOVE_NEXT_WORD_AT_BEGINNING, buffer));
          break;
        case BACKWARD_WORD:
          console.on(new KeyEvent(operation, EditorAction.MOVE_PREV_WORD_AT_BEGINNING, buffer));
          break;
        case VI_CHANGE_TO:
          if (buffer.length > 0 && buffer[0] == 'C') {
            // Workaround since it is only implemented in jline 2.12 with Operation.VI_CHANGE_TO_EOL
            // see https://github.com/jline/jline2/commit/f60432ffbd8322f53abb2d284e1f92f94acf0cc8
            console.setMode(new Status.Emacs()); // This is a hack to let the cursor until end of line
            console.on(new KeyEvent(operation, EditorAction.DELETE_END, buffer));
            console.setMode(new Status.Insert());
          } else {
            console.setMode(new ChangeTo());
          }
          break;
        case VI_YANK_TO:
          console.setMode(new YankTo());
          break;
        case VI_ARG_DIGIT:
          Digit digit = new Digit();
          digit.count = buffer[0] - '0';
          console.setMode(digit);
          break;
        case VI_APPEND_MODE:
          // That's a trick to let the cursor go to the end of the line
          // then we set to VI_INSERT
          console.setMode(new Status.Emacs());
          console.on(new KeyEvent(operation, EditorAction.RIGHT));
          console.setMode(new Status.Insert());
          break;
        case VI_BEGNNING_OF_LINE_OR_ARG_DIGIT:
          console.on(new KeyEvent(operation, EditorAction.MOVE_BEGINNING));
          break;
        case FORWARD_CHAR:
          console.on(new KeyEvent(operation, EditorAction.RIGHT));
          break;
        case TRANSPOSE_CHARS:
          console.on(new KeyEvent(operation, EditorAction.TRANSPOSE_CHARS));
          break;
        case UNIX_LINE_DISCARD:
          console.on(new KeyEvent(operation, EditorAction.DELETE_BEGINNING));
          break;
        case UNIX_WORD_RUBOUT:
          console.on(new KeyEvent(operation, EditorAction.DELETE_PREV_WORD));
          break;
        case END_OF_LINE:
          console.on(new KeyEvent(operation, EditorAction.MOVE_END));
          break;
        case VI_PREV_WORD:
          console.on(new KeyEvent(operation, EditorAction.MOVE_PREV_WORD_AT_BEGINNING));
          break;
        case BACKWARD_CHAR:
          console.on(new KeyEvent(operation, EditorAction.LEFT));
          break;
        case VI_END_WORD:
          console.on(new KeyEvent(operation, EditorAction.MOVE_NEXT_WORD_BEFORE_END));
          break;
        case VI_CHANGE_CASE:
          console.on(new KeyEvent(operation, EditorAction.CHANGE_CASE));
          break;
        case VI_SUBST:
          if (buffer.length > 0 && buffer[0] == 'S') {
            // Workaround since it is only implemented in jline 2.12 with Operation.KILL_WHOLE_LINE
            // see https://github.com/jline/jline2/commit/f60432ffbd8322f53abb2d284e1f92f94acf0cc8
            console.on(new KeyEvent(operation, EditorAction.DELETE_LINE, buffer));
            console.setMode(new Status.Insert());
          } else {
            super.on(console, operation, buffer);
          }
          break;
        case VI_PUT:
          console.on(new KeyEvent(operation, EditorAction.PASTE_AFTER, buffer));
          break;
        case VI_CHANGE_CHAR:
          console.setMode(new ChangeChar(1));
          break;
        case INTERRUPT:
          console.on(new KeyEvent(operation, EditorAction.INTERRUPT, buffer));
          break;
        case VI_SEARCH:
          // Unmapped
          break;
        case PREVIOUS_HISTORY:
          console.on(new KeyEvent(operation, EditorAction.HISTORY_PREV, buffer));
          break;
        case NEXT_HISTORY:
          console.on(new KeyEvent(operation, EditorAction.HISTORY_NEXT, buffer));
          break;
        case BEGINNING_OF_HISTORY:
          console.on(new KeyEvent(operation, EditorAction.HISTORY_FIRST, buffer));
          break;
        case END_OF_HISTORY:
          console.on(new KeyEvent(operation, EditorAction.HISTORY_LAST, buffer));
          break;
        case CLEAR_SCREEN:
          console.on(new KeyEvent(operation, EditorAction.CLS, buffer));
          break;
        default:
          super.on(console, operation, buffer);
      }
    }
  }

  public static class DeleteTo extends Status {

    @Override
    public String getKeyMap() {
      return "vi-move";
    }

    @Override
    public void on(Console console, Operation operation, int... buffer) {
      switch (operation) {
        case BACKWARD_CHAR:
          console.on(new KeyEvent(operation, EditorAction.DELETE_PREV_CHAR));
          break;
        case FORWARD_CHAR:
          console.on(new KeyEvent(operation, EditorAction.DELETE_NEXT_CHAR));
          break;
        case END_OF_LINE:
          console.on(new KeyEvent(operation, EditorAction.DELETE_END));
          break;
        case VI_NEXT_WORD:
          console.on(new KeyEvent(operation, EditorAction.DELETE_UNTIL_NEXT_WORD));
          break;
        case VI_DELETE_TO:
          console.on(new KeyEvent(operation, EditorAction.DELETE_LINE));
          break;
        case INTERRUPT:
          console.on(new KeyEvent(operation, EditorAction.INTERRUPT, buffer));
          console.toMove();
          break;
        default:
          break;
      }
      console.setMode(new Move());
    }
  }

  public static class ChangeTo extends Status {

    @Override
    public String getKeyMap() {
      return "vi-move";
    }

    @Override
    public void on(Console console, Operation operation, int... buffer) {
      switch (operation) {
        case BACKWARD_CHAR:
          console.on(new KeyEvent(operation, EditorAction.DELETE_PREV_CHAR));
          console.setMode(new Insert());
          break;
        case END_OF_LINE:
          console.setMode(new Status.Emacs()); // Hack
          console.on(new KeyEvent(operation, EditorAction.DELETE_END));
          console.setMode(new Insert());
          break;
        case VI_NEXT_WORD:
          console.on(new KeyEvent(operation, EditorAction.DELETE_NEXT_WORD));
          console.setMode(new Insert());
          break;
        case VI_CHANGE_TO:
          console.on(new KeyEvent(operation, EditorAction.DELETE_LINE));
          console.setMode(new Insert());
          break;
        case INTERRUPT:
          console.on(new KeyEvent(operation, EditorAction.INTERRUPT, buffer));
          console.toMove();
          break;
        default:
          console.setMode(new Move());
      }
    }
  }

  public static class YankTo extends Status {

    @Override
    public String getKeyMap() {
      return "vi-move";
    }


    @Override
    public void on(Console console, Operation operation, int... buffer) {
      switch (operation) {
        case END_OF_LINE:
          // Not implemented
          console.setMode(new Move());
          break;
        case INTERRUPT:
          console.on(new KeyEvent(operation, EditorAction.INTERRUPT, buffer));
          console.toMove();
          break;
        default:
          super.on(console, operation, buffer);
      }
    }
  }

  public static class ChangeChar extends Status {

    @Override
    public String getKeyMap() {
      return "vi-insert"; // We use insert for ESC
    }

    /** / */
    final int count;

    ChangeChar(int count) {
      this.count = count;
    }

    @Override
    public void on(Console console, Operation operation, int... buffer) {
      switch (operation) {
        case VI_MOVEMENT_MODE: // ESC
          console.setMode(new Move());
          break;
        case INTERRUPT:
          console.on(new KeyEvent(operation, EditorAction.INTERRUPT, buffer));
          console.toMove();
          break;
        default:
          console.on(new KeyEvent(operation, new EditorAction.ChangeChars(count, buffer[0])));
          console.setMode(new Move());
          break;
      }
    }
  }

  public static class Digit extends Status {

    /** . */
    int count = 0;

    /** . */
    Character to = null; // null | d:delete-to

    @Override
    public String getKeyMap() {
      return "vi-move";
    }

    @Override
    public void on(Console console, Operation operation, int... buffer) {
      switch (operation) {
        case VI_ARG_DIGIT:
          count = count * 10 + buffer[0] - '0';
          break;
        case BACKWARD_CHAR:
          if (to == null) {
            for (int i = 0;i < count;i++) {
              console.on(new KeyEvent(operation, EditorAction.LEFT));
            }
            console.setMode(new Move());
          } else if (to == 'd') {
            for (int i = 0;i < count;i++) {
              console.on(new KeyEvent(operation, EditorAction.DELETE_PREV_CHAR));
            }
            console.setMode(new Move());
          } else if (to == 'c') {
            for (int i = 0;i < count;i++) {
              console.on(new KeyEvent(operation, EditorAction.DELETE_PREV_CHAR));
            }
            console.setMode(new Insert());
          } else if (to == 'y') {
            // Not implemented
            console.setMode(new Move());
          } else {
            throw new AssertionError();
          }
          break;
        case FORWARD_CHAR:
          if (to == null) {
            for (int i = 0;i < count;i++) {
              console.on(new KeyEvent(operation, EditorAction.RIGHT));
            }
            console.setMode(new Move());
          } else if (to == 'd') {
            for (int i = 0;i < count;i++) {
              console.on(new KeyEvent(operation, EditorAction.DELETE_NEXT_CHAR));
            }
            console.setMode(new Move());
          } else if (to == 'c') {
            for (int i = 0;i < count;i++) {
              console.on(new KeyEvent(operation, EditorAction.DELETE_NEXT_CHAR));
            }
            console.setMode(new Insert());
          } else if (to == 'y') {
            throw new UnsupportedOperationException("Not yet handled");
          } else {
            super.on(console, operation, buffer);
          }
          console.setMode(new Move());
          break;
        case VI_NEXT_WORD:
          if (to == null) {
            for (int i = 0;i < count;i++) {
              console.on(new KeyEvent(operation, EditorAction.MOVE_NEXT_WORD_AT_BEGINNING));
            }
            console.setMode(new Move());
          } else if (to == 'd') {
            for (int i = 0;i < count;i++) {
              console.on(new KeyEvent(operation, EditorAction.DELETE_UNTIL_NEXT_WORD));
            }
            console.setMode(new Move());
          } else if (to == 'c') {
            for (int i = 0;i < count;i++) {
              console.on(new KeyEvent(operation, EditorAction.DELETE_NEXT_WORD));
            }
            console.setMode(new Insert());
          } else {
            super.on(console, operation, buffer);
          }
          break;
        case VI_PREV_WORD:
          if (to == null) {
            for (int i = 0;i < count;i++) {
              console.on(new KeyEvent(operation, EditorAction.MOVE_PREV_WORD_AT_END));
            }
            console.setMode(new Move());
          } else {
            super.on(console, operation, buffer);
          }
          break;
        case VI_END_WORD:
          if (to == null) {
            for (int i = 0;i < count;i++) {
              console.on(new KeyEvent(operation, EditorAction.MOVE_NEXT_WORD_BEFORE_END));
            }
            console.setMode(new Move());
          } else {
            super.on(console, operation, buffer);
          }
          break;
        case BACKWARD_DELETE_CHAR:
          if (to == null) {
            for (int i = 0;i < count;i++) {
              console.on(new KeyEvent(operation, EditorAction.DELETE_PREV_CHAR));
            }
            console.setMode(new Move());
          } else {
            super.on(console, operation, buffer);
          }
          break;
        case VI_CHANGE_CASE:
          if (to == null) {
            for (int i = 0;i < count;i++) {
              console.on(new KeyEvent(operation, EditorAction.CHANGE_CASE));
            }
            console.setMode(new Move());
          } else {
            super.on(console, operation, buffer);
          }
          break;
        case VI_DELETE:
          if (to == null) {
            console.on(new KeyEvent(operation, new EditorAction.DeleteNextChars(count)));
            console.setMode(new Move());
          } else {
            super.on(console, operation, buffer);
          }
          break;
        case VI_DELETE_TO:
          if (to != null) {
            throw new UnsupportedOperationException("Not yet handled");
          }
          to = 'd';
          break;
        case VI_CHANGE_TO:
          if (to != null) {
            throw new UnsupportedOperationException("Not yet handled");
          }
          to = 'c';
          break;
        case VI_YANK_TO:
          if (to != null) {
            throw new UnsupportedOperationException("Not yet handled");
          }
          to = 'y';
          break;
        case VI_CHANGE_CHAR:
          if (to != null) {
            throw new UnsupportedOperationException("Not yet handled");
          }
          console.setMode(new ChangeChar(count));
          break;
        case INTERRUPT:
          console.on(new KeyEvent(operation, EditorAction.INTERRUPT, buffer));
          console.toMove();
          break;
        default:
          console.setMode(new Move());
          break;
      }
    }
  }
}
