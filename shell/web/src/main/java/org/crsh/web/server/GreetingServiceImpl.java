package org.crsh.web.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.crsh.shell.Shell;
import org.crsh.shell.concurrent.SyncShellResponseContext;
import org.crsh.shell.impl.CRaSH;
import org.crsh.standalone.Bootstrap;
import org.crsh.web.client.GreetingService;
import org.crsh.web.shared.FieldVerifier;

/** The server side implementation of the RPC service. */
@SuppressWarnings("serial")
public class GreetingServiceImpl extends RemoteServiceServlet implements
   GreetingService
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

  public String greetServer(String input) throws IllegalArgumentException
   {
      // Verify that the input is valid.
      if (!FieldVerifier.isValidName(input))
      {
         // If the input is not valid, throw an IllegalArgumentException back to
         // the client.
         throw new IllegalArgumentException(
            "Name must be at least 4 characters long");
      }

      String serverInfo = getServletContext().getServerInfo();
      String userAgent = getThreadLocalRequest().getHeader("User-Agent");

      // Escape data from the client to avoid cross-site script vulnerabilities.
      input = escapeHtml(input);
      userAgent = escapeHtml(userAgent);

      return "Hello, " + input + "!<br><br>I am running " + serverInfo
         + ".<br><br>It looks like you are using:<br>" + userAgent;
   }

   /**
    * Escape an html string. Escaping data received from the client helps to prevent cross-site script vulnerabilities.
    *
    * @param html the html string to escape
    * @return the escaped string
    */
   private String escapeHtml(String html)
   {
      if (html == null)
      {
         return null;
      }
      return html.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(
         ">", "&gt;");
   }
}
