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

import org.crsh.util.Utils;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * <p>The current mode of the editor state machine. It decodes a command line operation according
 * to the current status and its possible state and provide an editor action that will modify the
 * state of the editor.</p>
 *
 * @author Julien Viet
 */
public abstract class Mode extends EditorAction {

  /** The logger. */
  private static final Logger log = Logger.getLogger(Mode.class.getName());

  public abstract String getKeyMap();

  public abstract String toString();

  @Override
  void perform(Editor editor, EditorBuffer buffer) throws IOException {
    editor.console.setMode(this);
  }

  /**
   * Transform a key stroke into a editor action. If no action must be taken, null should be returned.
   *
   * @param keyStroke the key stroke
   * @return the editor action
   */
  public EditorAction on(KeyStroke keyStroke) {
    String message = "Operation " + keyStroke.operation + " not mapped in " + getClass().getSimpleName() + " mode " + this;
    log.warning(message);
    return null;
  }

  public static final Mode EMACS = new Mode() {

    @Override
    public final String getKeyMap() {
      return "emacs";
    }

    @Override
    public EditorAction on(KeyStroke keyStroke) {
      switch (keyStroke.operation) {
        case SELF_INSERT:
          return new InsertKey(keyStroke.sequence);
        case VI_EDITING_MODE:
          return VI_INSERT;
        case BACKWARD_DELETE_CHAR:
          return EditorAction.DELETE_PREV_CHAR;
        case BACKWARD_CHAR:
          return EditorAction.LEFT;
        case FORWARD_CHAR:
          return EditorAction.RIGHT;
        case DELETE_CHAR:
          return EditorAction.DELETE_NEXT_CHAR;
        case BACKWARD_WORD:
          return EditorAction.MOVE_PREV_WORD_AT_BEGINNING;
        case FORWARD_WORD:
          return EditorAction.MOVE_NEXT_WORD_AFTER_END;
        case BEGINNING_OF_LINE:
          return EditorAction.MOVE_BEGINNING;
        case EXIT_OR_DELETE_CHAR:
          return EditorAction.EOF_MAYBE;
        case END_OF_LINE:
          return EditorAction.MOVE_END;
        case COMPLETE:
          return EditorAction.COMPLETE;
        case ACCEPT_LINE:
          return EditorAction.ENTER;
        case KILL_LINE:
          return EditorAction.DELETE_END;
        case BACKWARD_KILL_LINE:
          return EditorAction.DELETE_BEGINNING;
        case PREVIOUS_HISTORY:
          return EditorAction.HISTORY_PREV;
        case NEXT_HISTORY:
          return EditorAction.HISTORY_NEXT;
        case TRANSPOSE_CHARS:
          return EditorAction.TRANSPOSE_CHARS;
        case UNIX_LINE_DISCARD:
          return EditorAction.UNIX_LINE_DISCARD;
        case UNIX_WORD_RUBOUT:
          return EditorAction.DELETE_PREV_WORD;
        case BACKWARD_KILL_WORD:
          return EditorAction.DELETE_PREV_WORD;
        case INSERT_COMMENT:
          return EditorAction.INSERT_COMMENT;
        case BEGINNING_OF_HISTORY:
          return EditorAction.HISTORY_FIRST;
        case END_OF_HISTORY:
          return EditorAction.HISTORY_LAST;
        case INTERRUPT:
          return EditorAction.INTERRUPT;
        case CLEAR_SCREEN:
          return EditorAction.CLS;
        case YANK:
          return EditorAction.PASTE_AFTER;
        case KILL_WORD:
          return EditorAction.DELETE_NEXT_WORD;
        case DO_LOWERCASE_VERSION:
        case ABORT:
        case EXCHANGE_POINT_AND_MARK:
        case QUOTED_INSERT:
        case REVERSE_SEARCH_HISTORY:
        case FORWARD_SEARCH_HISTORY:
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
        default:
          return super.on(keyStroke);
      }
    }

    @Override
    public String toString() {
      return "Mode.EMACS";
    }
  };

  public static final Mode VI_INSERT = new Mode() {

    @Override
    public final String getKeyMap() {
      return "vi-insert";
    }

    @Override
    public EditorAction on(KeyStroke keyStroke) {
      switch (keyStroke.operation) {
        case VI_MOVEMENT_MODE:
          return VI_MOVE.then(EditorAction.LEFT);
        case FORWARD_CHAR:
          return EditorAction.RIGHT;
        case BACKWARD_CHAR:
          return EditorAction.LEFT;
        case VI_NEXT_WORD:
          return EditorAction.MOVE_NEXT_WORD_AT_BEGINNING;
        case VI_EOF_MAYBE:
          return EditorAction.EOF_MAYBE;
        case SELF_INSERT:
          return new InsertKey(keyStroke.sequence);
        case BACKWARD_DELETE_CHAR:
          return EditorAction.DELETE_PREV_CHAR;
        case COMPLETE:
          return EditorAction.COMPLETE;
        case ACCEPT_LINE:
          return EditorAction.ENTER;
        case TRANSPOSE_CHARS:
          return EditorAction.TRANSPOSE_CHARS;
        case UNIX_LINE_DISCARD:
          return EditorAction.UNIX_LINE_DISCARD;
        case UNIX_WORD_RUBOUT:
          return EditorAction.DELETE_PREV_WORD;
        case INTERRUPT:
          return EditorAction.INTERRUPT;
        case PREVIOUS_HISTORY:
          return EditorAction.HISTORY_PREV;
        case NEXT_HISTORY:
          return EditorAction.HISTORY_NEXT;
        case BEGINNING_OF_HISTORY:
          return EditorAction.HISTORY_FIRST;
        case END_OF_HISTORY:
          return EditorAction.HISTORY_LAST;
        case YANK:
        case MENU_COMPLETE:
        case MENU_COMPLETE_BACKWARD:
        case REVERSE_SEARCH_HISTORY:
        case FORWARD_SEARCH_HISTORY:
        case QUOTED_INSERT:
        case UNDO:
          // Not yet implemented
        default:
          return super.on(keyStroke);
      }
    }

    @Override
    public String toString() {
      return "Mode.VI_INSERT";
    }
  };

  public static final Mode VI_MOVE = new Mode() {

    @Override
    public final String getKeyMap() {
      return "vi-move";
    }

    @Override
    public EditorAction on(KeyStroke keyStroke) {
      int[] buffer = keyStroke.sequence;
      switch (keyStroke.operation) {
        case VI_MOVE_ACCEPT_LINE:
          return EditorAction.ENTER;
        case VI_INSERTION_MODE:
          return VI_INSERT;
        case VI_INSERT_BEG:
          return EditorAction.MOVE_BEGINNING.then(VI_INSERT);
        case VI_INSERT_COMMENT:
          return EditorAction.INSERT_COMMENT;
        case BACKWARD_DELETE_CHAR:
          return EditorAction.DELETE_PREV_CHAR;
        case VI_DELETE:
          return EditorAction.DELETE_NEXT_CHAR;
        case KILL_LINE:
          return EditorAction.DELETE_END;
        case BACKWARD_KILL_LINE:
          return EditorAction.DELETE_BEGINNING;
        case VI_DELETE_TO_EOL:
          return EditorAction.DELETE_END;
        case VI_DELETE_TO:
          return DELETE_TO;
        case VI_NEXT_WORD:
          return EditorAction.MOVE_NEXT_WORD_AT_BEGINNING;
        case BACKWARD_WORD:
          return EditorAction.MOVE_PREV_WORD_AT_BEGINNING;
        case VI_CHANGE_TO_EOL:
          return EMACS.then(EditorAction.DELETE_END).then(VI_INSERT);
        case VI_CHANGE_TO:
          return CHANGE_TO;
        case VI_YANK_TO:
          return YANK_TO;
        case VI_ARG_DIGIT:
          Digit digit = new Digit();
          digit.count = buffer[0] - '0';
          return digit;
        case VI_APPEND_MODE:
          // That's a trick to let the cursor go to the end of the line
          // then we set to VI_INSERT
          return EMACS.then(EditorAction.RIGHT).then(VI_INSERT);
        case VI_BEGINNING_OF_LINE_OR_ARG_DIGIT:
          return EditorAction.MOVE_BEGINNING;
        case FORWARD_CHAR:
          return EditorAction.RIGHT;
        case TRANSPOSE_CHARS:
          return EditorAction.TRANSPOSE_CHARS;
        case UNIX_LINE_DISCARD:
          return EditorAction.UNIX_LINE_DISCARD;
        case UNIX_WORD_RUBOUT:
          return EditorAction.DELETE_PREV_WORD;
        case END_OF_LINE:
          return EditorAction.MOVE_END;
        case VI_PREV_WORD:
          return EditorAction.MOVE_PREV_WORD_AT_BEGINNING;
        case BACKWARD_CHAR:
          return EditorAction.LEFT;
        case VI_END_WORD:
          return EditorAction.MOVE_NEXT_WORD_BEFORE_END;
        case VI_CHANGE_CASE:
          return EditorAction.CHANGE_CASE;
        case VI_KILL_WHOLE_LINE:
          return EditorAction.DELETE_LINE.then(VI_INSERT);
        case VI_PUT:
          return EditorAction.PASTE_AFTER;
        case VI_CHANGE_CHAR:
          return new ChangeChar(1);
        case INTERRUPT:
          return EditorAction.INTERRUPT;
        case VI_SEARCH:
          // Unmapped
          return null;
        case PREVIOUS_HISTORY:
          return EditorAction.HISTORY_PREV;
        case NEXT_HISTORY:
          return EditorAction.HISTORY_NEXT;
        case BEGINNING_OF_HISTORY:
          return EditorAction.HISTORY_FIRST;
        case END_OF_HISTORY:
          return EditorAction.HISTORY_LAST;
        case CLEAR_SCREEN:
          return EditorAction.CLS;
        default:
          return super.on(keyStroke);
      }
    }

    @Override
    public String toString() {
      return "Mode.VI_MOVE";
    }
  };

  public static final Mode DELETE_TO =  new Mode() {

    @Override
    public String getKeyMap() {
      return "vi-move";
    }

    @Override
    public EditorAction on(KeyStroke keyStroke) {
      switch (keyStroke.operation) {
        case BACKWARD_CHAR:
          return EditorAction.DELETE_PREV_CHAR.then(VI_MOVE);
        case FORWARD_CHAR:
          return EditorAction.DELETE_NEXT_CHAR.then(VI_MOVE);
        case END_OF_LINE:
          return EditorAction.DELETE_END.then(VI_MOVE);
        case VI_NEXT_WORD:
          return EditorAction.DELETE_UNTIL_NEXT_WORD.then(VI_MOVE);
        case VI_DELETE_TO:
          return EditorAction.DELETE_LINE.then(VI_MOVE);
        case INTERRUPT:
          return EditorAction.INTERRUPT.then(VI_MOVE);
        default:
          return VI_MOVE;
      }
    }

    @Override
    public String toString() {
      return "Mode.DELETE_TO";
    }
  };

  public static final Mode CHANGE_TO = new Mode() {

    @Override
    public String getKeyMap() {
      return "vi-move";
    }

    @Override
    public EditorAction on(KeyStroke keyStroke) {
      switch (keyStroke.operation) {
        case BACKWARD_CHAR:
          return EditorAction.DELETE_PREV_CHAR.then(VI_INSERT);
        case END_OF_LINE:
          return EMACS.then(EditorAction.DELETE_END).then(VI_INSERT);
        case VI_NEXT_WORD:
          return EditorAction.DELETE_NEXT_WORD.then(VI_INSERT);
        case VI_CHANGE_TO:
          return EditorAction.DELETE_LINE.then(VI_INSERT);
        case INTERRUPT:
          return EditorAction.INTERRUPT.then(VI_MOVE);
        default:
          return VI_MOVE;
      }
    }

    @Override
    public String toString() {
      return "Mode.CHANGE_TO";
    }
  };

  public static final Mode YANK_TO = new Mode() {

    @Override
    public String getKeyMap() {
      return "vi-move";
    }


    @Override
    public EditorAction on(KeyStroke keyStroke) {
      switch (keyStroke.operation) {
        case VI_YANK_TO:
          return EditorAction.COPY.then(VI_MOVE);
        case END_OF_LINE:
          return COPY_END_OF_LINE.then(VI_MOVE);
        case VI_BEGINNING_OF_LINE_OR_ARG_DIGIT:
          return COPY_BEGINNING_OF_LINE.then(VI_MOVE);
        case VI_NEXT_WORD:
          return EditorAction.COPY_NEXT_WORD.then(VI_MOVE);
        case VI_FIRST_PRINT:
          return EditorAction.COPY_PREV_WORD.then(VI_MOVE);
        case INTERRUPT:
          return EditorAction.INTERRUPT.then(VI_MOVE);
        default:
          return super.on(keyStroke);
      }
    }

    @Override
    public String toString() {
      return "Mode.YANK_TO";
    }
  };

  public static class ChangeChar extends Mode {

    @Override
    public String getKeyMap() {
      return "vi-insert"; // We use insert for ESC
    }

    /** / */
    final int count;

    public ChangeChar(int count) {
      this.count = count;
    }

    @Override
    public EditorAction on(KeyStroke keyStroke) {
      switch (keyStroke.operation) {
        case VI_MOVEMENT_MODE: // ESC
          return VI_MOVE;
        case INTERRUPT:
          return EditorAction.INTERRUPT.then(VI_MOVE);
        default:
          return new EditorAction.ChangeChars(count, keyStroke.sequence[0]).then(VI_MOVE);
      }
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      } else if (obj instanceof ChangeChar) {
        ChangeChar that = (ChangeChar)obj;
        return count == that.count;
      } else {
        return false;
      }
    }

    @Override
    public String toString() {
      return "Mode.ChangeChat[count=" + count + "]";
    }
  }

  public static class Digit extends Mode {

    /** . */
    int count = 0;

    /** . */
    Character to = null; // null | d:delete-to

    public Digit(int count) {
      this.count = count;
    }

    public Digit() {
      this(0);
    }

    public int getCount() {
      return count;
    }

    public Character getTo() {
      return to;
    }

    @Override
    public String getKeyMap() {
      return "vi-move";
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      } else if (obj instanceof Digit) {
        Digit that = (Digit)obj;
        return count == that.count && Utils.equals(to, that.to);
      } else {
        return false;
      }
    }

    @Override
    public String toString() {
      return "Mode.Digit[count=" + count + ",to=" + to + "]";
    }

    @Override
    public EditorAction on(KeyStroke keyStroke) {
      switch (keyStroke.operation) {
        case VI_ARG_DIGIT:
          count = count * 10 + keyStroke.sequence[0] - '0';
          return null;
        case BACKWARD_CHAR:
          if (to == null) {
            return EditorAction.LEFT.repeat(count).then(VI_MOVE);
          } else if (to == 'd') {
            return EditorAction.DELETE_PREV_CHAR.repeat(count).then(VI_MOVE);
          } else if (to == 'c') {
            return EditorAction.DELETE_PREV_CHAR.repeat(count).then(VI_INSERT);
          } else if (to == 'y') {
            // Not implemented
            return VI_MOVE;
          } else {
            throw new AssertionError();
          }
        case FORWARD_CHAR:
          if (to == null) {
            return EditorAction.RIGHT.repeat(count).then(VI_MOVE);
          } else if (to == 'd') {
            return EditorAction.DELETE_NEXT_CHAR.repeat(count).then(VI_MOVE);
          } else if (to == 'c') {
            return EditorAction.DELETE_NEXT_CHAR.repeat(count).then(VI_INSERT);
          } else if (to == 'y') {
            throw new UnsupportedOperationException("Not yet handled");
          } else {
            return super.on(keyStroke);
          }
        case VI_NEXT_WORD:
          if (to == null) {
            return EditorAction.MOVE_NEXT_WORD_AT_BEGINNING.repeat(count).then(VI_MOVE);
          } else if (to == 'd') {
            return EditorAction.DELETE_UNTIL_NEXT_WORD.repeat(count).then(VI_MOVE);
          } else if (to == 'c') {
            return EditorAction.DELETE_NEXT_WORD.repeat(count).then(VI_INSERT);
          } else {
            return super.on(keyStroke);
          }
        case VI_PREV_WORD:
          if (to == null) {
            return EditorAction.MOVE_PREV_WORD_AT_END.repeat(count).then(VI_MOVE);
          } else {
            super.on(keyStroke);
          }
        case VI_END_WORD:
          if (to == null) {
            return EditorAction.MOVE_NEXT_WORD_BEFORE_END.repeat(count).then(VI_MOVE);
          } else {
            super.on(keyStroke);
          }
        case BACKWARD_DELETE_CHAR:
          if (to == null) {
            return EditorAction.DELETE_PREV_CHAR.repeat(count).then(VI_MOVE);
          } else {
            return super.on(keyStroke);
          }
        case VI_CHANGE_CASE:
          if (to == null) {
            return EditorAction.CHANGE_CASE.repeat(count).then(VI_MOVE);
          } else {
            return super.on(keyStroke);
          }
        case VI_DELETE:
          if (to == null) {
            return new EditorAction.DeleteNextChars(count).then(VI_MOVE);
          } else {
            return super.on(keyStroke);
          }
        case VI_DELETE_TO:
          if (to != null) {
            throw new UnsupportedOperationException("Not yet handled");
          }
          to = 'd';
          return null;
        case VI_CHANGE_TO:
          if (to != null) {
            throw new UnsupportedOperationException("Not yet handled");
          }
          to = 'c';
          return null;
        case VI_YANK_TO:
          return YANK_TO;
        case VI_CHANGE_CHAR:
          if (to != null) {
            throw new UnsupportedOperationException("Not yet handled");
          }
          return new ChangeChar(count);
        case INTERRUPT:
          return EditorAction.INTERRUPT.then(VI_MOVE);
        default:
          return VI_MOVE;
      }
    }
  }
}
