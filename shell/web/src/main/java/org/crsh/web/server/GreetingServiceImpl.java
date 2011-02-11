package org.crsh.web.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.crsh.shell.Shell;
import org.crsh.shell.concurrent.SyncShellResponseContext;
import org.crsh.shell.impl.CRaSH;
import org.crsh.standalone.Bootstrap;
import org.crsh.web.client.ShellService;

/** The server side implementation of the RPC service. */
@SuppressWarnings("serial")
public class GreetingServiceImpl extends RemoteServiceServlet implements
  ShellService
{

   public GreetingServiceImpl()
   {
   }

   public GreetingServiceImpl(Object delegate)
   {
      super(delegate);
   }

   {
      Shell shell = null;
      try
      {
         Bootstrap bootstrap = new Bootstrap();
         bootstrap.bootstrap();
         shell = new CRaSH(bootstrap.getContext());
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }

      this.shell = shell;
   }

   /** . */
   private final Shell shell;

  public String getWelcome() {
    return shell.getWelcome() + "\n" + shell.getPrompt();
  }

  public String process(String s) {
    StringBuilder sb = new StringBuilder();
    try {
      SyncShellResponseContext resp = new SyncShellResponseContext();
      shell.process(s, resp);
      String text = resp.getResponse().getText();
      sb.append(text);
    }
    catch (InterruptedException e) {
      e.printStackTrace();
      sb.append("failure ").append(e.getMessage());
    }
    sb.append('\n').append(shell.getPrompt());
    return sb.toString();
  }
}
