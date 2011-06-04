package org.crsh.cmdline.matcher.tokenizer;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
class State {

  /** . */
  StringBuilder buffer;

  /** . */
  Escape escape;

  /** . */
  Status status;

  public State() {
    this.buffer = new StringBuilder();
    this.status = Status.INIT;
    this.escape = Escape.NONE;
  }

  void push(char c) {

    //
    switch (escape) {
      case NONE:
        if (c == '"') {
          escape = Escape.DOUBLE;
          return;
        } else if (c == '\\') {
          escape = Escape.BACKSLASH;
          return;
        } else if (c == '\'') {
          escape = Escape.SINGLE;
          return;
        } else {
          // Do nothing
          break;
        }
      case DOUBLE:
        if (c == '"') {
          escape = Escape.NONE;
          return;
        } else {
          // Do nothing
          break;
        }
      case SINGLE:
        if (c == '\'') {
          escape = Escape.NONE;
          return;
        } else {
          // Do nothing
          break;
        }
      case BACKSLASH:
        escape = Escape.NONE;
        break;
      default:
        throw new AssertionError(escape);
    }

    switch (status) {
      case INIT: {
        if (c == '-') {
          buffer.append(c);
          status = Status.SHORT_OPTION;
          return;
        } else {
          buffer.append(c);
          status = Status.WORD;
          return;
        }
      }
      case WORD: {
        buffer.append(c);
        status = Status.WORD;
        return;
      }
      case SHORT_OPTION: {
        if (Character.isLetter(c)) {
          buffer.append(c);
          return;
        } else if (c == '-') {
          buffer.append('-');
          status = Status.LONG_OPTION;
          return;
        } else {
          buffer.append(c);
          status = Status.WORD;
          return;
        }
      }
      case LONG_OPTION: {
        if (Character.isLetter(c)) {
          buffer.append(c);
          return;
        } else {
          buffer.append(c);
          status = Status.WORD;
          return;
        }
      }
      default:
        throw new AssertionError(escape);
    }
  }
}
