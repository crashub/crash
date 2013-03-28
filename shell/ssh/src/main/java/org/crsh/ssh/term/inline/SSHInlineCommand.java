package org.crsh.ssh.term.inline;

import org.apache.sshd.server.Environment;
import org.crsh.plugin.PluginContext;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellFactory;
import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellResponse;
import org.crsh.ssh.term.AbstractCommand;
import org.crsh.ssh.term.SSHLifeCycle;

import java.io.IOException;
import java.security.Principal;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SSH inline command
 */
public class SSHInlineCommand extends AbstractCommand implements Runnable {

    protected final Logger log = Logger.getLogger(getClass().getName());

    protected static final int OK = 0;

    protected static final int ERROR = 2;

    private Thread thread;

    private Environment env;

    private String command;

    private PluginContext pluginContext;

    private SSHInlineShellProcessContext pipeShellProcessContext;

    public SSHInlineCommand(String command, PluginContext pluginContext) {
        this.command = command;
        this.pluginContext = pluginContext;
    }

    public void start(Environment environment) throws IOException {
        this.env = env;

        //
        thread = new Thread(this, "CRaSH");
        thread.start();
    }

    public void destroy() {
        thread.interrupt();
    }

    public void run() {
        int exitStatus = OK;
        String exitMsg = null;

        //
        try {
            execute();
        }
        catch (Exception e) {
            log.log(Level.SEVERE, "Error during command execution", e);
            exitMsg = e.getMessage();
            exitStatus = ERROR;
        }
        finally {
            // get command output
            if(pipeShellProcessContext.getResponse() instanceof ShellResponse.Ok) {
                try {
                    out.write(pipeShellProcessContext.getOutput().getBytes());
                    out.flush();
                } catch (IOException ioe) {
                    log.log(Level.SEVERE, "Error during command output reading", ioe);
                }
            } else {
                String errorMsg = null;
                if(pipeShellProcessContext.getResponse() != null) {
                    errorMsg = "Error during command execution : " + pipeShellProcessContext.getResponse().getMessage();
                } else {
                    errorMsg =  "Error during command execution";
                }
                log.log(Level.SEVERE, errorMsg);
            }

            // Say we are done
            if (callback != null) {
                callback.onExit(exitStatus, exitMsg);
            }
        }
    }

    public void execute() {
        // get principal
        final String userName = session.getAttribute(SSHLifeCycle.USERNAME);
        Principal user = new Principal() {
            public String getName() {
                return userName;
            }
        };

        Shell shell = pluginContext.getPlugin(ShellFactory.class).create(user);
        ShellProcess shellProcess = shell.createProcess(command);
        pipeShellProcessContext = SSHInlineShellProcessContext.create(shellProcess);
        shellProcess.execute(pipeShellProcessContext);
    }
}
