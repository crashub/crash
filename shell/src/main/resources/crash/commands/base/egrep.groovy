package crash.commands.base

import org.crsh.cli.Argument
import org.crsh.cli.Command
import org.crsh.cli.Usage
import org.crsh.command.Pipe
import org.crsh.text.Screenable
import org.crsh.text.ScreenContext
import org.crsh.text.Style
import org.crsh.util.Utils

import java.util.regex.Matcher
import java.util.regex.Pattern

class egrep {

  /** . */
  static final char CR = '\n';


  static class impl extends Pipe<Void, Object> implements ScreenContext {

    /** . */
    final Matcher matcher;

    /** . */
    StringBuffer buffer = new StringBuffer()

    /** . */
    ArrayList<Object> chunks = new ArrayList<Object>()

    impl(Matcher matcher) {
      this.matcher = matcher
    }

    @Override
    void provide(Void element) throws ScriptException, IOException {
      // Ignore
    }

    @Override
    int getWidth() {
      return context.getWidth();
    }

    @Override
    int getHeight() {
      return context.getHeight();
    }

    @Override
    Appendable append(char c) throws IOException {
      return append("" + c);
    }

    @Override
    Appendable append(CharSequence text) throws IOException {
      int prev = 0;
      while (true) {
        int index = Utils.indexOf(text, prev, CR);
        if (index == -1) {
          def rest = text.subSequence(prev, text.length())
          buffer.append(rest);
          chunks.add(rest);
          break;
        } else {
          def s = text.subSequence(prev, index + 1)
          buffer.append(s);
          chunks.add(s);
          def matched = matcher.reset(buffer).find();
          chunks.findAll{ matched || !(it instanceof CharSequence) }.each(context.&provide)
          chunks.clear();
          buffer.setLength(0);
          prev = index + 1;
        }
      }
      return this;
    }

    @Override
    Appendable append(CharSequence csq, int start, int end) throws IOException {
      return append(csq.subSequence(start, end));
    }

    @Override
    Screenable append(Style style) throws IOException {
      chunks.add(style);
      return this;
    }

    @Override
    Screenable cls() throws IOException {
      buffer.setLength(0);
      chunks.clear();
      context.cls();
      return this;
    }

    @Override
    void close() throws org.crsh.command.ScriptException {
      matcher.reset(buffer)
      if (matcher.find()) {
        chunks.each(context.&provide);
        context.flush();
      }
      super.close();
    }

  }

  @Usage("search file(s) for lines that match a pattern")
  @Command
  Pipe<Void, Object> main(@Argument @Usage("the search pattern") String pattern) {
    if (pattern == null) {
      pattern = "";
    }
    return new impl(Pattern.compile(pattern).matcher(""));
  }
}