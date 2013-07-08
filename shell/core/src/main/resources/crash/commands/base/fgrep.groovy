package crash.commands.base

import org.crsh.cli.Argument
import org.crsh.cli.Command
import org.crsh.cli.Usage
import org.crsh.command.PipeCommand
import org.crsh.text.Chunk
import org.crsh.text.Text
import org.crsh.text.CLS

import java.util.regex.Pattern

class fgrep {
    @Usage("print lines matching a pattern")
    @Command
    PipeCommand<Chunk, Chunk> main(@Argument String regex) {
        return new PipeCommand<Chunk, Chunk>() {
            StringBuffer buffer = new StringBuffer()
            ArrayList<Chunk> chunks = new ArrayList<>()

            @Override
            void provide(Chunk element) {
                if(element instanceof CLS){
                    context.provide(element)
                    return
                }

                chunks.add(element)

                if(element instanceof Text){
                    if(isLineComplete(element)){
                        fgrep()
                        clear()
                    }
                    else
                        buffer.append(element.text)
                }
            }

            private void clear() {
                chunks.clear()
                buffer = new StringBuffer()
            }

            private boolean isLineComplete(Text element) {
                element.text.contains("\n")
            }

            private void fgrep() {
                if (buffer.toString().contains(regex))
                    chunks.each { chunk -> context.provide(chunk) }
            }
        }
    }
}