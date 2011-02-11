package org.crsh.web.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import java.util.List;

/** The client side stub for the RPC service. */
@RemoteServiceRelativePath("greet")
public interface ShellService extends RemoteService {

  String getWelcome();

  String process(String s);

  List<String> complete(String s);
}
