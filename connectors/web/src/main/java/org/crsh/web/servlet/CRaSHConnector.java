/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.crsh.web.servlet;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.crsh.cli.impl.Delimiter;
import org.crsh.cli.impl.completion.CompletionMatch;
import org.crsh.cli.spi.Completion;
import org.crsh.command.ShellSafety;
import org.crsh.command.ShellSafetyFactory;
import org.crsh.keyboard.KeyType;
import org.crsh.plugin.PluginContext;
import org.crsh.plugin.WebPluginLifeCycle;
import org.crsh.shell.Shell;
import org.crsh.shell.ShellFactory;
import org.crsh.shell.ShellProcess;
import org.crsh.util.Utils;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.net.URI;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/** @author Julien Viet */
@ServerEndpoint(value = "/crash", configurator = Configurator.class)
public class CRaSHConnector {

  /** . */
  static final Logger log = Logger.getLogger(CRaSHConnector.class.getName());

  /** . */
  private final ConcurrentHashMap<String, CRaSHSession> sessions = new ConcurrentHashMap<String, CRaSHSession>();

  /** . */
  private static final ThreadLocal<Session> current = new ThreadLocal<Session>();

  /**
   * @return the current session crash id (CRASHID) or null if none is associated with the request
   */
  public static String getHttpSessionId() {
    Session session = current.get();
    if (session != null) {
      return (String)session.getUserProperties().get("CRASHID");
    } else {
      return null;
    }
  }

  @OnOpen
  public void start(Session wsSession) {
    current.set(wsSession);
    try {
      URI uri = wsSession.getRequestURI();
      String path = uri.getPath();
      log.fine("Establishing session for " + path);
      String contextPath = path.substring(0, path.lastIndexOf('/'));
      PluginContext context = WebPluginLifeCycle.getPluginContext(contextPath);
      if (context != null) {
        Boolean enabled = context.getProperty(WebPlugin.ENABLED);
        if (enabled != null && enabled) {
          log.fine("Using shell " + context);
          ShellFactory factory = context.getPlugin(ShellFactory.class);
          Principal user = wsSession.getUserPrincipal();
          Shell shell = factory.create(user, null, ShellSafetyFactory.getCurrentThreadShellSafety());
          CRaSHSession session = new CRaSHSession(wsSession, shell);
          sessions.put(wsSession.getId(), session);
          log.fine("Established session " + wsSession.getId());
        } else {
          log.fine("Web plugin disabled");
        }
      } else {
        log.fine("No shell found");
      }
    }
    finally {
      current.set(null);
    }
  }

  @OnClose
  public void end(Session wsSession) {
    current.set(wsSession);
    try {
      CRaSHSession session = sessions.remove(wsSession.getId());
      if (session != null) {
        log.fine("Destroying session " + wsSession.getId());
        WSProcessContext current = session.current.getAndSet(null);
        if (current != null) {
          log.fine("Cancelling on going command " + current.command + " for " + wsSession.getId());
          current.process.cancel();
        }
      } else {
        log.fine("No shell session found");
      }
    }
    finally {
      current.set(null);
    }
  }

  @OnMessage
  public void incoming(String message, Session wsSession) {
    String key = wsSession.getId();
    log.fine("Received message " + message + " from session " + key);
    current.set(wsSession);
    try {
      CRaSHSession session = sessions.get(key);
      if (session != null) {
        JsonParser parser = new JsonParser();
        JsonElement json = parser.parse(message);
        if (json instanceof JsonObject) {
          JsonObject event = (JsonObject)json;
          JsonElement type = event.get("type");
          if (type.getAsString().equals("welcome")) {
            log.fine("Sending welcome + prompt");
            session.send("print", session.shell.getWelcome());
            session.send("prompt", session.shell.getPrompt());
          } else if (type.getAsString().equals("execute")) {
            String command = event.get("command").getAsString();
            int width = event.get("width").getAsInt();
            int height = event.get("height").getAsInt();
            ShellProcess process = session.shell.createProcess(command);
            WSProcessContext context = new WSProcessContext(session, process, command, width, height);
            if (session.current.getAndSet(context) == null) {
              log.fine("Executing \"" + command + "\"");
              process.execute(context);
            } else {
              log.fine("Could not execute \"" + command + "\"");
            }
          } else if (type.getAsString().equals("cancel")) {
            WSProcessContext current = session.current.getAndSet(null);
            if (current != null) {
              log.fine("Cancelling command \"" + current.command + "\"");
              current.process.cancel();
            } else {
              log.fine("No process to cancel");
            }
          } else if (type.getAsString().equals("key")) {
            WSProcessContext current = session.current.get();
            if (current != null) {
              String _keyType = event.get("keyType").getAsString();
              KeyType keyType = KeyType.valueOf(_keyType.toUpperCase());
              if (keyType == KeyType.CHARACTER) {
                int code = event.get("keyCode").getAsInt();
                if (code >= 32) {
                  current.handle(KeyType.CHARACTER, new int[]{code});
                }
              } else {
                current.handle(keyType, new int[0]);
              }
            } else {
              log.fine("No process can handle the key event");
            }
          } else if (type.getAsString().equals("complete")) {
            String prefix = event.get("prefix").getAsString();
            CompletionMatch completion = session.shell.complete(prefix);
            Completion completions = completion.getValue();
            Delimiter delimiter = completion.getDelimiter();
            StringBuilder sb = new StringBuilder();
            List<String> values = new ArrayList<String>();
            try {
              if (completions.getSize() == 1) {
                String value = completions.getValues().iterator().next();
                delimiter.escape(value, sb);
                if (completions.get(value)) {
                  sb.append(delimiter.getValue());
                }
                values.add(sb.toString());
              }
              else {
                String commonCompletion = Utils.findLongestCommonPrefix(completions.getValues());
                if (commonCompletion.length() > 0) {
                  delimiter.escape(commonCompletion, sb);
                  values.add(sb.toString());
                }
                else {
                  for (Map.Entry<String, Boolean> entry : completions) {
                    delimiter.escape(entry.getKey(), sb);
                    values.add(sb.toString());
                    sb.setLength(0);
                  }
                }
              }
            }
            catch (IOException ignore) {
              // Should not happen
            }
            log.fine("Completing \"" + prefix + "\" with " + values);
            session.send("complete", values);
          }
        }
      } else {
        log.fine("No shell session found");
      }
    }
    finally {
      current.set(null);
    }
  }
}
