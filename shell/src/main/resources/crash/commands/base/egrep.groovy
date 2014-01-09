package crash.commands.base

import org.crsh.cli.Argument
import org.crsh.cli.Command
import org.crsh.cli.Usage
import org.crsh.command.PipeCommand
import org.crsh.text.CLS
import org.crsh.text.Chunk
import org.crsh.text.Text
import org.crsh.util.Utils

import java.util.regex.Matcher
import java.util.regex.Pattern

class egrep {

  /** . */
  static final char CR = '\n';

  @Usage("search file(s) for lines that match a pattern")
  @Command
  PipeCommand<Chunk, Chunk> main(@Argument @Usage("the search pattern") String pattern) {
    if (pattern == null) {
      pattern = "";
    }
    final Matcher matcher = Pattern.compile(pattern).matcher("");
    return new PipeCommand<Chunk, Chunk>() {

      /** . */
      StringBuffer buffer = new StringBuffer()

      /** . */
      ArrayList<Chunk> chunks = new ArrayList<>()

      @Override
      void provide(Chunk element) {
        if (element instanceof CLS) {
          buffer.setLength(0);
          chunks.clear();
          context.provide(element)
        } else if (element instanceof Text) {
          Text text = (Text)element;
          int prev = 0;
          while (true) {
            int index = Utils.indexOf(text.text, prev, CR);
            if (index == -1) {
              def rest = text.text.subSequence(prev, text.text.length())
              buffer.append(rest);
              chunks.add(Text.create(rest));
              break;
            } else {
              def s = text.text.subSequence(prev, index)
              buffer.append(s);
              chunks.add(Text.create(s));
              def matched = matcher.reset(buffer).find();
              chunks.findAll{ matched || !(it instanceof Text) }.each(context.&provide)
              chunks.clear();
              buffer.setLength(0);
              prev = index + 1;
            }
          }
        } else {
          chunks.add(element)
        }
      }

      @Override
      void close() throws org.crsh.command.ScriptException {
        if (buffer.contains(pattern)) {
          context.provide(Text.create(buffer));
          context.flush();
        }
        super.close();
      }
    }
  }
}