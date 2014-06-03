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

import org.crsh.cli.impl.Delimiter;
import org.crsh.cli.impl.completion.CompletionMatch;
import org.crsh.cli.impl.line.LineParser;
import org.crsh.cli.impl.line.MultiLineVisitor;
import org.crsh.cli.spi.Completion;
import org.crsh.util.Utils;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * An action on the editor.
 */
class EditorAction {

  static class InsertKey extends EditorAction {

    private final int[] sequence;

    public InsertKey(int[] sequence) {
      this.sequence = sequence;
    }

    void perform(Editor editor, EditorBuffer buffer) throws IOException {
      StringBuilder sb = new StringBuilder(sequence.length);
      for (int c : sequence) {
        sb.appendCodePoint(c);
      }
      buffer.append(sb);
    }
  }

  static EditorAction COMPLETE = new EditorAction() {
    @Override
    String execute(Editor editor, EditorBuffer buffer, int[] sequence, boolean flush) throws IOException {

      // Compute prefix
      MultiLineVisitor visitor = new MultiLineVisitor();
      LineParser parser = new LineParser(visitor);
      List<String> lines = buffer.getLines();
      for (int i = 0;i < lines.size();i++) {
        if (i > 0) {
          parser.crlf();
        }
        parser.append(lines.get(i));
      }
      String prefix = visitor.getRaw();

      // log.log(Level.FINE, "About to get completions for " + prefix);
      CompletionMatch completion = editor.console.shell.complete(prefix);
      // log.log(Level.FINE, "Completions for " + prefix + " are " + completions);

      //
      if (completion != null) {
        Completion completions = completion.getValue();

        //
        Delimiter delimiter = completion.getDelimiter();

        try {
          // Try to find the greatest prefix among all the results
          if (completions.getSize() == 0) {
            // Do nothing
          } else if (completions.getSize() == 1) {
            Map.Entry<String, Boolean> entry = completions.iterator().next();
            String insert = entry.getKey();
            StringBuilder sb = new StringBuilder();
            sb.append(delimiter.escape(insert));
            if (entry.getValue()) {
              sb.append(completion.getDelimiter().getValue());
            }
            buffer.append(sb);
            editor.console.driver.flush();
          } else {
            String commonCompletion = Utils.findLongestCommonPrefix(completions.getValues());

            // Format stuff
            int width = editor.console.driver.getWidth();

            //
            String completionPrefix = completions.getPrefix();

            // Get the max length
            int max = 0;
            for (String suffix : completions.getValues()) {
              max = Math.max(max, completionPrefix.length() + suffix.length());
            }

            // Separator : use two whitespace like in BASH
            max += 2;

            //
            StringBuilder sb = new StringBuilder().append('\n');
            if (max < width) {
              int columns = width / max;
              int index = 0;
              for (String suffix : completions.getValues()) {
                sb.append(completionPrefix).append(suffix);
                for (int l = completionPrefix.length() + suffix.length();l < max;l++) {
                  sb.append(' ');
                }
                if (++index >= columns) {
                  index = 0;
                  sb.append('\n');
                }
              }
              if (index > 0) {
                sb.append('\n');
              }
            } else {
              for (Iterator<String> i = completions.getValues().iterator();i.hasNext();) {
                String suffix = i.next();
                sb.append(commonCompletion).append(suffix);
                if (i.hasNext()) {
                  sb.append('\n');
                }
              }
              sb.append('\n');
            }

            // Add current buffer
            int index = 0;
            for (String line : lines) {
              if (index == 0) {
                String prompt = editor.console.shell.getPrompt();
                sb.append(prompt == null ? "" : prompt);
              } else {
                sb.append("\n> ");
              }
              sb.append(line);
              index++;
            }

            // Redraw everything
            editor.console.driver.write(sb.toString());

            // If we have common completion we append it now in the buffer
            if (commonCompletion.length() > 0) {
              buffer.append(delimiter.escape(commonCompletion));
            }

            // Flush
            buffer.flush(true);
          }
        }
        catch (IOException e) {
          // log.log(Level.SEVERE, "Could not write completion", e);
        }
      }

      //
      return null;
    }
  };

  static EditorAction INTERRUPT = new EditorAction() {
    @Override
    String execute(Editor editor, EditorBuffer buffer, int[] sequence, boolean flush) throws IOException {
      editor.lineParser.reset();
      buffer.reset();
      editor.console.driver.writeCRLF();
      String prompt = editor.console.shell.getPrompt();
      if (prompt != null) {
        editor.console.driver.write(prompt);
      }
      if (flush) {
        editor.console.driver.flush();
      }
      return null;
    }
  };

  static EditorAction EOF_MAYBE = new EditorAction() {
    @Override
    String execute(Editor editor, EditorBuffer buffer, int[] sequence, boolean flush) throws IOException {
      if (editor.isEmpty()) {
        editor.console.status = Console.CLOSING;
        return null;
      } else {
        if (editor.console.getMode() == Mode.EMACS) {
          return EditorAction.DELETE_PREV_CHAR.execute(editor, buffer, sequence, true);
        } else {
          return EditorAction.ENTER.execute(editor, buffer, sequence, true);
        }
      }
    }
  };

  public abstract static class History extends EditorAction {

    protected abstract int getNext(Editor editor);

    @Override
    void perform(Editor editor, EditorBuffer buffer) throws IOException {
      int nextHistoryCursor = getNext(editor);
      if (nextHistoryCursor >= -1 && nextHistoryCursor < editor.history.size()) {
        String s = nextHistoryCursor == -1 ? editor.historyBuffer : editor.history.get(nextHistoryCursor);
        while (buffer.moveRight()) {
          // Do nothing
        }
        String t = buffer.replace(s);
        if (editor.historyCursor == -1) {
          editor.historyBuffer = t;
        } else {
          editor.history.set(editor.historyCursor, t);
        }
        editor.historyCursor = nextHistoryCursor;
      }
    }
  }

  static EditorAction HISTORY_FIRST = new History() {
    @Override
    protected int getNext(Editor editor) {
      return editor.history.size() - 1;
    }
  };

  static EditorAction HISTORY_LAST = new History() {
    @Override
    protected int getNext(Editor editor) {
      return 0;
    }
  };

  static EditorAction HISTORY_PREV = new History() {
    @Override
    protected int getNext(Editor editor) {
      return editor.historyCursor + 1;
    }
  };

  static EditorAction HISTORY_NEXT = new History() {
    @Override
    protected int getNext(Editor editor) {
      return editor.historyCursor - 1;
    }
  };

  static EditorAction LEFT = new EditorAction() {
    @Override
    void perform(Editor editor, EditorBuffer buffer) throws IOException {
      buffer.moveLeft();
    }
  };

  static EditorAction RIGHT = new EditorAction() {
    @Override
    void perform(Editor editor, EditorBuffer buffer) throws IOException {
      if (buffer.getCursor() < editor.getCursorBound()) {
        buffer.moveRight();
      }
    }
  };

  static EditorAction MOVE_BEGINNING = new EditorAction() {
    @Override
    void perform(Editor editor, EditorBuffer buffer) throws IOException {
      int cursor = buffer.getCursor();
      if (cursor > 0) {
        buffer.moveLeftBy(cursor);
      }
    }
  };

  static class MovePrevWord extends EditorAction {

    final boolean atBeginning /* otherwise at end */;

    public MovePrevWord(boolean atBeginning) {
      this.atBeginning = atBeginning;
    }

    @Override
    void perform(Editor editor, EditorBuffer buffer) throws IOException {
      int cursor = buffer.getCursor();
      int pos = cursor;
      while (pos > 0) {
        char c = buffer.charAt(pos - 1);
        if ((atBeginning && Character.isLetterOrDigit(c)) || (!atBeginning && !Character.isLetterOrDigit(c))) {
          break;
        } else {
          pos--;
        }
      }
      while (pos > 0) {
        char c = buffer.charAt(pos - 1);
        if ((atBeginning && !Character.isLetterOrDigit(c)) || (!atBeginning && Character.isLetterOrDigit(c))) {
          break;
        } else {
          pos--;
        }
      }
      if (pos < cursor) {
        buffer.moveLeftBy(cursor - pos);
      }
    }
  }

  static EditorAction MOVE_PREV_WORD_AT_BEGINNING = new MovePrevWord(true);

  static EditorAction MOVE_PREV_WORD_AT_END = new MovePrevWord(false);

  static class MoveNextWord extends EditorAction {

    final At at;

    public MoveNextWord(At at) {
      this.at = at;
    }

    @Override
    void perform(Editor editor, EditorBuffer buffer) throws IOException {
      int to = editor.getCursorBound();
      int from = buffer.getCursor();
      int pos = from;
      while (true) {
        int look = at == At.BEFORE_END ? pos + 1 : pos;
        if (look < to) {
          char c = buffer.charAt(look);
          if ((at != At.BEGINNING && Character.isLetterOrDigit(c)) || (at == At.BEGINNING && !Character.isLetterOrDigit(c))) {
            break;
          } else {
            pos++;
          }
        } else {
          break;
        }
      }
      while (true) {
        int look = at == At.BEFORE_END ? pos + 1 : pos;
        if (look < to) {
          char c = buffer.charAt(look);
          if ((at != At.BEGINNING && !Character.isLetterOrDigit(c)) || (at == At.BEGINNING && Character.isLetterOrDigit(c))) {
            break;
          } else {
            pos++;
          }
        } else {
          break;
        }
      }
      if (pos > from) {
        buffer.moveRightBy(pos - from);
      }
    }
  }

  static EditorAction MOVE_NEXT_WORD_AT_BEGINNING = new MoveNextWord(At.BEGINNING);

  static EditorAction MOVE_NEXT_WORD_AFTER_END = new MoveNextWord(At.AFTER_END);

  static EditorAction MOVE_NEXT_WORD_BEFORE_END = new MoveNextWord(At.BEFORE_END);

  static EditorAction DELETE_PREV_WORD = new EditorAction() {
    @Override
    void perform(Editor editor, EditorBuffer buffer) throws IOException {
      editor.killBuffer.setLength(0);
      boolean chars = false;
      while (true) {
        int cursor = buffer.getCursor();
        if (cursor > 0) {
          if (buffer.charAt(cursor - 1) == ' ') {
            if (!chars) {
              editor.killBuffer.appendCodePoint(buffer.del());
            } else {
              break;
            }
          } else {
            editor.killBuffer.appendCodePoint(buffer.del());
            chars = true;
          }
        } else {
          break;
        }
      }
      editor.killBuffer.reverse();
    }
  };

  static EditorAction DELETE_NEXT_WORD = new EditorAction() {
    @Override
    void perform(Editor editor, EditorBuffer buffer) throws IOException {
      int count = 0;
      boolean chars = false;
      while (true) {
        if (buffer.getCursor() < buffer.getSize()) {
          char c = buffer.charAt(buffer.getCursor());
          if (!Character.isLetterOrDigit(c)) {
            if (!chars) {
              count++;
              buffer.moveRight();
            } else {
              break;
            }
          } else {
            chars = true;
            count++;
            buffer.moveRight();
          }
        } else {
          break;
        }
      }
      editor.killBuffer.setLength(0);
      while (count-- > 0) {
        editor.killBuffer.appendCodePoint(buffer.del());
      }
      editor.killBuffer.reverse();
    }
  };

  static EditorAction DELETE_UNTIL_NEXT_WORD = new EditorAction() {
    @Override
    void perform(Editor editor, EditorBuffer buffer) throws IOException {
      int pos = buffer.getCursor();
      EditorAction.MOVE_NEXT_WORD_AT_BEGINNING.perform(editor, buffer);
      while (buffer.getCursor() > pos) {
        buffer.del();
      }
    }
  };

  static EditorAction DELETE_END = new EditorAction() {
    @Override
    void perform(Editor editor, EditorBuffer buffer) throws IOException {
      int count = 0;
      while (buffer.moveRight()) {
        count++;
      }
      editor.killBuffer.setLength(0);
      while (count-- > 0) {
        editor.killBuffer.appendCodePoint(buffer.del());
      }
      editor.killBuffer.reverse();
      if (buffer.getCursor() > editor.getCursorBound()) {
        buffer.moveLeft();
      }
    }
  };

  static EditorAction DELETE_BEGINNING = new EditorAction() {
    @Override
    void perform(Editor editor, EditorBuffer buffer) throws IOException {
      editor.killBuffer.setLength(0);
      while (editor.buffer.getCursor() > 0) {
        editor.killBuffer.appendCodePoint(buffer.del());
      }
      editor.killBuffer.reverse();
    }
  };

  static EditorAction UNIX_LINE_DISCARD = new EditorAction() {
    @Override
    void perform(Editor editor, EditorBuffer buffer) throws IOException {
      // Not really efficient
      if (buffer.getCursor()  > 0) {
        editor.killBuffer.setLength(0);
        while (buffer.getCursor() > 0) {
          int c = buffer.del();
          editor.killBuffer.appendCodePoint(c);
        }
        editor.killBuffer.reverse();
      }
    }
  };

  static EditorAction DELETE_LINE = new EditorAction() {
    @Override
    String execute(Editor editor, EditorBuffer buffer, int[] sequence, boolean flush) throws IOException {
      buffer.moveRightBy(buffer.getSize() - buffer.getCursor());
      buffer.replace("");
      return null;
    }
  };

  static EditorAction PASTE_AFTER = new EditorAction() {
    @Override
    void perform(Editor editor, EditorBuffer buffer) throws IOException {
      if (editor.killBuffer.length() > 0) {
        for (int i = 0;i < editor.killBuffer.length();i++) {
          char c = editor.killBuffer.charAt(i);
          buffer.append(c);
        }
      }
    }
  };

  static EditorAction MOVE_END = new EditorAction() {
    @Override
    void perform(Editor editor, EditorBuffer buffer) throws IOException {
      int cursor = editor.getCursorBound() - buffer.getCursor();
      if (cursor > 0) {
        buffer.moveRightBy(cursor);
      }
    }
  };

  static abstract class Copy extends EditorAction {

    protected abstract int getFrom(EditorBuffer buffer);

    protected abstract int getTo(EditorBuffer buffer);

    @Override
    void perform(Editor editor, EditorBuffer buffer) throws IOException {
      int from = getFrom(buffer);
      int to = getTo(buffer);
      editor.killBuffer.setLength(0);
      for (int i = from;i < to;i++) {
        editor.killBuffer.append(editor.buffer.charAt(i));
      }
    }
  }

  static EditorAction COPY = new Copy() {
    @Override
    protected int getFrom(EditorBuffer buffer) {
      return 0;
    }
    @Override
    protected int getTo(EditorBuffer buffer) {
      return buffer.getSize();
    }
  };

  static EditorAction COPY_END_OF_LINE = new Copy() {
    @Override
    protected int getFrom(EditorBuffer buffer) {
      return buffer.getCursor();
    }
    @Override
    protected int getTo(EditorBuffer buffer) {
      return buffer.getSize();
    }
  };

  static EditorAction COPY_BEGINNING_OF_LINE = new Copy() {
    @Override
    protected int getFrom(EditorBuffer buffer) {
      return 0;
    }
    @Override
    protected int getTo(EditorBuffer buffer) {
      return buffer.getCursor();
    }
  };

  static EditorAction COPY_NEXT_WORD = new EditorAction() {
    @Override
    void perform(Editor editor, EditorBuffer buffer) throws IOException {
      int size = editor.buffer.getSize();
      int cursor = editor.buffer.getCursor();
      editor.killBuffer.setLength(0);
      while (cursor < size && editor.buffer.charAt(cursor) != ' ') {
        editor.killBuffer.append(editor.buffer.charAt(cursor++));
      }
      while (cursor < size && editor.buffer.charAt(cursor) == ' ') {
        editor.killBuffer.append(editor.buffer.charAt(cursor++));
      }
    }
  };

  static EditorAction COPY_PREV_WORD = new EditorAction() {
    @Override
    void perform(Editor editor, EditorBuffer buffer) throws IOException {
      int cursor = buffer.getCursor() - 1;
      editor.killBuffer.setLength(0);
      while (cursor > 0 && buffer.charAt(cursor) != ' ') {
        editor.killBuffer.append(buffer.charAt(cursor--));
      }
      while (cursor > 0 && editor.buffer.charAt(cursor) == ' ') {
        editor.killBuffer.append(buffer.charAt(cursor--));
      }
      editor.killBuffer.reverse();
    }
  };

  static class ChangeChars extends EditorAction {

    /** . */
    public final int count;

    /** . */
    public final int c;

    public ChangeChars(int count, int c) {
      this.count = count;
      this.c = c;
    }

    @Override
    void perform(Editor editor, EditorBuffer buffer) throws IOException {
      int a = Math.min(count, buffer.getSize() - buffer.getCursor());
      while (a-- > 0) {
        buffer.moveRight((char)c);
      }
      buffer.moveLeft();
    }
  }

  static EditorAction DELETE_PREV_CHAR = new EditorAction() {
    @Override
    void perform(Editor editor, EditorBuffer buffer) throws IOException {
      buffer.del();
    }
  };

  static class DeleteNextChars extends EditorAction {

    /** . */
    public final int count;

    public DeleteNextChars(int count) {
      this.count = count;
    }

    @Override
    void perform(Editor editor, EditorBuffer buffer) throws IOException {
      int tmp = count;
      while (tmp > 0 && buffer.moveRight()) {
        tmp--;
      }
      while (tmp++ < count) {
        buffer.del();
      }
      if (buffer.getCursor() > editor.getCursorBound()) {
        buffer.moveLeft();
      }
    }
  }

  static EditorAction DELETE_NEXT_CHAR = ((EditorAction)new DeleteNextChars(1));

  static EditorAction CHANGE_CASE = new EditorAction() {
    @Override
    void perform(Editor editor, EditorBuffer buffer) throws IOException {
      if (buffer.getCursor() < buffer.getSize()) {
        char c = buffer.charAt(buffer.getCursor());
        if (Character.isUpperCase(c)) {
          c = Character.toLowerCase(c);
        }
        else if (Character.isLowerCase(c)) {
          c = Character.toUpperCase(c);
        }
        buffer.moveRight(c);
        if (buffer.getCursor() > editor.getCursorBound()) {
          buffer.moveLeft();
        }
      }
    }
  };

  static EditorAction TRANSPOSE_CHARS = new EditorAction() {
    @Override
    void perform(Editor editor, EditorBuffer buffer) throws IOException {
      if (buffer.getSize() > 2) {
        int pos = buffer.getCursor();
        if (pos > 0) {
          if (pos < buffer.getSize()) {
            if (buffer.moveLeft()) {
              char a = buffer.charAt(pos - 1);
              char b = buffer.charAt(pos);
              buffer.moveRight(b); // Should be assertion
              buffer.moveRight(a); // Should be assertion
              // A bit not great : need to find a better way to do that...
              if (editor.console.getMode() == Mode.VI_MOVE && buffer.getCursor() > editor.getCursorBound()) {
                buffer.moveLeft();
              }
            }
          } else {
            if (buffer.moveLeft() && buffer.moveLeft()) {
              char a = buffer.charAt(pos - 2);
              char b = buffer.charAt(pos - 1);
              buffer.moveRight(b); // Should be assertion
              buffer.moveRight(a); // Should be assertion
            }
          }
        }
      }
    }
  };

  static EditorAction INSERT_COMMENT = new EditorAction() {
    @Override
    String execute(Editor editor, EditorBuffer buffer, int[] sequence, boolean flush) throws IOException {
      EditorAction.MOVE_BEGINNING.perform(editor, buffer);
      buffer.append("#");
      return EditorAction.ENTER.execute(editor, buffer, sequence, flush);
    }
  };

  static EditorAction CLS = new EditorAction() {
    @Override
    void perform(Editor editor, EditorBuffer buffer) throws IOException {
      editor.console.driver.cls();
      StringBuilder sb = new StringBuilder();
      int index = 0;
      List<String> lines = buffer.getLines();
      for (String line : lines) {
        if (index == 0) {
          String prompt = editor.console.shell.getPrompt();
          sb.append(prompt == null ? "" : prompt);
        } else {
          sb.append("\n> ");
        }
        sb.append(line);
        index++;
      }
      editor.console.driver.write(sb.toString());
      editor.console.driver.flush();
    }
  };

  static EditorAction ENTER = new EditorAction() {
    @Override
    String execute(Editor editor, EditorBuffer buffer, int[] sequence, boolean flush) throws IOException {
      editor.historyCursor = -1;
      editor.historyBuffer = null;
      String line = buffer.getLine();
      editor.lineParser.append(line);
      if (editor.console.getMode() == Mode.VI_MOVE) {
        editor.console.setMode(Mode.VI_INSERT);
      }
      if (editor.lineParser.crlf()) {
        editor.console.driver.writeCRLF();
        editor.console.driver.flush();
        String request = editor.visitor.getRaw();
        if (request.length() > 0) {
          editor.addToHistory(request);
        }
        return request;
      } else {
        buffer.append('\n');
        editor.console.driver.write("> ");
        if (flush) {
          buffer.flush();
        }
        return null;
      }
    }
  };

  String execute(Editor editor, EditorBuffer buffer, int[] sequence, boolean flush) throws IOException {
    perform(editor, buffer);
    if (flush) {
      buffer.flush();
    }
    return null;
  }

  void perform(Editor editor, EditorBuffer buffer) throws IOException {
    throw new UnsupportedOperationException("Implement the edition logic");
  }

  public EditorAction then(final EditorAction action) {
    return new EditorAction() {
      @Override
      String execute(Editor editor, EditorBuffer buffer, int[] sequence, boolean flush) throws IOException {
        EditorAction.this.execute(editor, buffer, sequence, flush);
        return action.execute(editor, buffer, sequence, flush);
      }
    };
  }

  public EditorAction repeat(final int count) {
    return new EditorAction() {
      @Override
      void perform(Editor editor, EditorBuffer buffer) throws IOException {
        for (int i = 0;i < count;i++) {
          EditorAction.this.perform(editor, buffer);
        }
      }
    };
  }
}
