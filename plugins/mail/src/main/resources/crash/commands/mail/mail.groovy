package crash.commands.mail

import org.crsh.cli.Argument
import org.crsh.cli.Command
import org.crsh.cli.Man
import org.crsh.cli.Option
import org.crsh.cli.Usage
import org.crsh.command.Pipe
import org.crsh.mail.MailPlugin
import org.crsh.shell.impl.command.CRaSH
import org.crsh.text.Screenable
import org.crsh.text.ScreenBuffer
import org.crsh.text.Format
import org.crsh.text.ScreenContext
import org.crsh.text.Style

import java.util.concurrent.ExecutionException
import java.util.concurrent.Future


@Usage("interact with emails")
class mail {

  class impl extends Pipe<Object, Object> implements ScreenContext {

    impl(CRaSH crash, Integer width, Integer height, Boolean block, String subject, List<String> recipients) {
      this.crash = crash;
      this.width = width;
      this.height = height;
      this.block = block;
      this.subject = subject;
      this.recipients = recipients;
    }

    final CRaSH crash;
    final Integer width;
    final Integer height;
    final Boolean block;
    final String subject;
    final List<String> recipients;
    ScreenBuffer buffer;
    MailPlugin plugin;

    @Override
    int getWidth() {
      return width != null ? width : context.getWidth();
    }

    @Override
    int getHeight() {
      return height != null ? height : context.getHeight();
    }

    @Override
    Appendable append(char c) throws IOException {
      buffer.append(c);
      return this;
    }

    @Override
    Appendable append(CharSequence s) throws IOException {
      buffer.append(s);
      return this;
    }

    @Override
    Appendable append(CharSequence csq, int start, int end) throws IOException {
      buffer.append(csq, start, end);
      return this;
    }

    @Override
    Screenable append(Style style) throws IOException {
      buffer.append(style);
      return this;
    }

    @Override
    Screenable cls() throws IOException {
      buffer.cls();
      return this;
    }

    @Override
    void open() throws ScriptException {
      buffer = new ScreenBuffer();
      plugin = crash.context.getPlugin(MailPlugin.class);
    }

    @Override
    void provide(Object element) {
      context.provide(element)
    }

    @Override
    void close() throws org.crsh.command.ScriptException {
      if (recipients != null && recipients.size() > 0) {
        StringBuilder sb = new StringBuilder()
        buffer.format(Format.PRE_HTML, sb);
        Future<Boolean> future = plugin.send(recipients, subject, sb.toString(), "text/html;charset=UTF-8");
        if (block) {
          try {
            future.get();
          }
          catch (ExecutionException e) {
            throw new org.crsh.command.ScriptException(e);
          }
        }
      }
      super.close()
    }

  }

  @Man("""Send an mail to a list of recipients.

The body of the mail is the input stream of the command. For example, the output of the "thread ls | thread dump" command
can be piped into the mail command: an email with the list of current JVM thread is sent to the admin:

% thread ls | thread dump | mail send -s "The thread dump" admin@foo.com
""")
  @Usage("send an mail to a list of recipients, the body of the mail is the input stream of the command.")
  @Command
  Pipe<Object, Object> send(
      @Usage("block until the mails are delivered")
      @Option(names=["b","block"])
      Boolean block,
      @Usage("mail subject")
      @Option(names=["s","subject"])
      String subject,
      @Usage("override the screen width")
      @Option(names = ["width"])
      Integer width,
      @Usage("override the screen height")
      @Option(names = ["height"])
      Integer height,
      @Usage("mail recipients")
      @Argument List<String> recipients) {
    if (width != null && width < 1) {
      throw new org.crsh.command.ScriptException("Invalid screen width: $width")
    }
    if (height != null && height < 1) {
      throw new org.crsh.command.ScriptException("Invalid screen height: $height")
    }
    return new impl(crash, width, height, block, subject, recipients);
  }
}