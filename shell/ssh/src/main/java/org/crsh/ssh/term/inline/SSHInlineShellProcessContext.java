package org.crsh.ssh.term.inline;

import org.crsh.shell.Shell;
import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellProcessContext;
import org.crsh.shell.ShellResponse;
import org.crsh.text.Chunk;
import org.crsh.text.Text;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;

/**
 * ShellProcessContext for SSH inline commands
 */
public class SSHInlineShellProcessContext implements ShellProcessContext {
    public static SSHInlineShellProcessContext create(Shell shell, String line) {
        return new SSHInlineShellProcessContext(shell, line);
    }

    public static SSHInlineShellProcessContext create(ShellProcess process) {
        return new SSHInlineShellProcessContext(process);
    }

    /** . */
    private final LinkedList<String> output = new LinkedList<String>();

    /** . */
    private final LinkedList<String> input = new LinkedList<String>();

    /** . */
    private ShellResponse response;

    /** . */
    private final CountDownLatch latch;

    /** . */
    private int width;

    /** . */
    private int height;

    /** . */
    private ShellProcess process;

    private SSHInlineShellProcessContext(ShellProcess process) {
        this.process = process;
        this.latch = new CountDownLatch(1);
        this.response = null;
        this.width = 0;
        this.height = 0;
    }

    private SSHInlineShellProcessContext(Shell shell, String line) {
        this(shell.createProcess(line));
    }

    public SSHInlineShellProcessContext cancel() {
        process.cancel();
        return this;
    }

    public SSHInlineShellProcessContext execute() {
        process.execute(this);
        return this;
    }

    public ShellProcess getProcess() {
        return process;
    }

    public SSHInlineShellProcessContext addLineInput(String line) {
        input.add(line);
        return this;
    }


    public boolean takeAlternateBuffer() {
        return false;
    }

    public boolean releaseAlternateBuffer() {
        return false;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getProperty(String name) {
        return null;
    }

    public String readLine(String msg, boolean echo) {
        output.addLast(msg);
        return input.isEmpty() ? null : input.removeLast();
    }

    public Class<Chunk> getConsumedType() {
        return Chunk.class;
    }

    public void provide(Chunk element) throws IOException {
        if (element instanceof Text) {
            CharSequence seq = ((Text)element).getText();
            if (seq.length() > 0) {
                output.add(seq.toString());
            }
        }
    }

    public String getOutput() {
        StringBuilder buffer = new StringBuilder();
        for (String o : output) {
            buffer.append(o);
        }
        return buffer.toString();
    }

    public void flush() {
    }

    public void end(ShellResponse response) {
        this.response = response;
        this.latch.countDown();
    }

    public ShellResponse getResponse() {
        try {
            latch.await();
            return response;
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
