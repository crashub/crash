package org.crsh.web.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import java.util.List;
import java.util.Map;

/** The client side stub for the RPC service. */
@RemoteServiceRelativePath("shell")
public interface ShellService extends RemoteService {

  String getWelcome();

  String process(String s);

  Map<String, String> complete(String s);
}
