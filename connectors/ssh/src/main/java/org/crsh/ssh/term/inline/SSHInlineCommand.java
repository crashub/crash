package org.crsh.ssh.term.inline;

import org.apache.sshd.server.Environment;
import org.apache.sshd.server.channel.ChannelSession;
import org.crsh.auth.AuthInfo;
import org.crsh.command.ShellSafety;
import org.crsh.command.ShellSafetyFactory;
import org.crsh.plugin.PluginContext;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellFactory;
import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellResponse;
import org.crsh.ssh.term.AbstractCommand;
import org.crsh.ssh.term.SSHContext;
import org.crsh.ssh.term.SSHLifeCycle;
import org.crsh.auth.DisconnectPlugin;

import java.io.IOException;
import java.io.PrintStream;
import java.security.Principal;
import java.util.logging.Level;
import java.util.logging.Logger;

/** SSH inline command */
public class SSHInlineCommand extends AbstractCommand implements Runnable {

  /** . */
  protected static final Logger log = Logger.getLogger(SSHInlineCommand.class.getName());

  /** . */
  protected static final int OK = 0;

  /** . */
  protected static final int ERROR = 2;

  /** . */
  private Thread thread;

  /** . */
  private String command;

  /** . */
  private PluginContext pluginContext;

  /** . */
  private Environment env;

  public SSHInlineCommand(String command, PluginContext pluginContext) {
    this.command = command;
    this.pluginContext = pluginContext;
  }

  public void start(ChannelSession channel, Environment environment) throws IOException {
    this.env = environment;
    thread = new Thread(this, "CRaSH");
    thread.start();
  }

  // Called only for SSH clients using <command> option, i.e. without interactive shell.
  public void destroy(ChannelSession channel) {
    DisconnectPlugin disconnectHandler = pluginContext.getPlugin(DisconnectPlugin.class);
    if (disconnectHandler != null) {
      final String userName = session.getAttribute(SSHLifeCycle.USERNAME);
      AuthInfo authInfo = session.getAttribute(SSHLifeCycle.AUTH_INFO);
      disconnectHandler.onDisconnect(userName, authInfo);
      log.info("Session " + userName + "@" + session.getIoSession().getRemoteAddress() + " disconnected");
    }
    thread.interrupt();
  }

  public void run() {

    // get principal
    PrintStream err = new PrintStream(this.err);
    PrintStream out = new PrintStream(this.out);
    final String userName = session.getAttribute(SSHLifeCycle.USERNAME);
    AuthInfo authInfo = session.getAttribute(SSHLifeCycle.AUTH_INFO);
    Principal user = new Principal() {
      public String getName() {
        return userName;
      }
    };
    Shell shell = pluginContext.getPlugin(ShellFactory.class).create(user, authInfo, ShellSafetyFactory.getCurrentThreadShellSafety());
    ShellProcess shellProcess = shell.createProcess(command);

    //
    SSHInlineShellProcessContext context = new SSHInlineShellProcessContext(new SSHContext(env), shellProcess, out, err);
    int exitStatus = OK;
    String exitMsg = null;

    //
    try {
      shellProcess.execute(context);
    }
    catch (Exception e) {
      log.log(Level.SEVERE, "Error during command execution", e);
      exitMsg = e.getMessage();
      exitStatus = ERROR;
    }
    finally {
      // get command output
      ShellResponse response = context.getResponse();
      if (response instanceof ShellResponse.Ok) {
        // Ok
      }
      else {
        String errorMsg;

        // Set the exit status to Error
        exitStatus = ERROR;

        if (response != null) {
          errorMsg = "Error during command execution : " + response.getMessage();
        }
        else {
          errorMsg = "Error during command execution";
        }
        err.println(errorMsg);
        if (response instanceof ShellResponse.Error) {
          ShellResponse.Error error = (ShellResponse.Error)response;
          log.log(Level.SEVERE, errorMsg, error.getThrowable());
        } else {
          log.log(Level.SEVERE, errorMsg);
        }
      }

      // Say we are done
      if (callback != null) {
        callback.onExit(exitStatus, exitMsg);
      }
    }
  }
}
