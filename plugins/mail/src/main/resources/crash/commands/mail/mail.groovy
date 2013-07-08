package crash.commands.mail

import org.crsh.cli.Argument
import org.crsh.cli.Command
import org.crsh.cli.Option
import org.crsh.cli.Usage
import org.crsh.command.PipeCommand
import org.crsh.mail.MailPlugin
import org.crsh.text.Chunk
import org.crsh.text.ChunkBuffer
import org.crsh.text.Format

import java.util.concurrent.ExecutionException
import java.util.concurrent.Future


class mail {
  @Usage("mail input text")
  @Command
  PipeCommand<Chunk, Chunk> main(
      @Usage("block until the mails are delivered")
      @Option(names=["b","block"])
      Boolean block,
      @Usage("mail subject")
      @Option(names=["s","subject"])
      String subject,
      @Usage("mail recipients")
      @Argument List<String> recipients) {
    ChunkBuffer buffer = new ChunkBuffer();
    MailPlugin plugin = crash.context.getPlugin(MailPlugin.class);
    new PipeCommand<Chunk, Chunk>() {

      @Override
      void provide(Chunk element) {
        buffer.write(element);
        context.provide(element)
      }

      @Override
      void close() throws org.crsh.command.ScriptException {
        StringBuilder sb = new StringBuilder()
        sb.append("<pre>");
        buffer.format(Format.TEXT, sb);
        sb.append("</pre>");
        Future<Boolean> future = plugin.send(recipients, subject, sb.toString(), "text/html;charset=UTF-8");
        if (block) {
          try {
            future.get();
          }
          catch (ExecutionException e) {
            throw new org.crsh.command.ScriptException(e);
          }
        }
        super.close()
      }
    }
  }
}