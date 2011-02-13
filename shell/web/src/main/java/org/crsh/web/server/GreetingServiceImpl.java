package org.crsh.web.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.crsh.shell.Shell;
import org.crsh.shell.concurrent.SyncShellResponseContext;
import org.crsh.shell.impl.CRaSH;
import org.crsh.standalone.Bootstrap;
import org.crsh.util.Strings;
import org.crsh.web.client.ShellService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/** The server side implementation of the RPC service. */
@SuppressWarnings("serial")
public class GreetingServiceImpl extends RemoteServiceServlet implements ShellService {

  /** . */
  private static final Logger log = LoggerFactory.getLogger(GreetingServiceImpl.class);

  public GreetingServiceImpl() {
  }

  public GreetingServiceImpl(Object delegate) {
    super(delegate);
  }

  {
    Shell shell = null;
    try {
      Bootstrap bootstrap = new Bootstrap();
      bootstrap.bootstrap();
      shell = new CRaSH(bootstrap.getContext());
    }
    catch (Exception e) {
      log.error("Bootstrap failed", e);
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

  public Map<String, String> complete(String s) {

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
