package org.crsh.web.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.crsh.plugin.PluginContext;
import org.crsh.plugin.WebPluginLifeCycle;
import org.crsh.shell.Shell;
import org.crsh.shell.concurrent.SyncShellResponseContext;
import org.crsh.shell.impl.CRaSH;
import org.crsh.standalone.Bootstrap;
import org.crsh.util.Strings;
import org.crsh.web.client.ShellService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/** The server side implementation of the RPC service. */
@SuppressWarnings("serial")
public class ShellServiceImpl extends RemoteServiceServlet implements ShellService {

  /** . */
  private static final Logger log = LoggerFactory.getLogger(ShellServiceImpl.class);

  /** . */
  private PluginContext pluginContext;

  public ShellServiceImpl() {
  }

  public ShellServiceImpl(Object delegate) {
    super(delegate);
  }

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    // Obtain plugin context for our servlet context
    pluginContext = WebPluginLifeCycle.getPluginContext(config.getServletContext());

    // We assume it's a demo and we bootstrap something
    if (pluginContext == null) {
      try {
        Bootstrap bootstrap = new Bootstrap(Thread.currentThread().getContextClassLoader());
        bootstrap.bootstrap();
        pluginContext = bootstrap.getContext();
      } catch (Exception e) {
        log.error("Bootstrap failed", e);
      }
    } else {
      log.info("Obtained plugin context " + pluginContext);
    }
  }

  /** . */
//  private final Shell shell;

  private Shell getShell() {
    HttpServletRequest req = this.getThreadLocalRequest();
    HttpSession session = req.getSession();
    CRaSH shell = (CRaSH)session.getAttribute(ShellServiceImpl.class.getName());
    if (shell == null) {
      log.debug("Created shell");
      shell = new CRaSH(pluginContext);
      session.setAttribute(ShellServiceImpl.class.getName(), shell);
    }
    return shell;
  }

  public String getWelcome() {
    Shell shell = getShell();
    return shell.getWelcome() + "\n" + shell.getPrompt();
  }

  public String process(String s) {
    Shell shell = getShell();
    StringBuilder sb = new StringBuilder();
    try {
      SyncShellResponseContext resp = new SyncShellResponseContext();
      shell.createProcess(s).execute(resp);
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

  public Map<String, String> complete(String s) {
    Shell shell = getShell();

    // Obtain completions from the shell
    Map<String, String> completions = shell.complete(s);

    // Try to find the greatest prefix among all the results
    String commonCompletion;
    if (completions.size() == 0) {
      commonCompletion = "";
    }
    else if (completions.size() == 1) {
      Map.Entry<String, String> entry = completions.entrySet().iterator().next();
      commonCompletion = entry.getKey() + entry.getValue();
    }
    else {
      commonCompletion = Strings.findLongestCommonPrefix(completions.keySet());
    }

    // Use our hashmap so we are sure it will be correctly serialized
    Map<String, String> ret = new HashMap<String, String>();
    if (commonCompletion.length() > 0) {
      ret.put(commonCompletion, "");
    }
    else {
      if (completions.size() > 1) {
        ret.putAll(completions);
      }
    }
    return ret;
  }
}
