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

import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Julien Viet
 */
public class Configurator extends ServerEndpointConfig.Configurator {

  /** . */
  private static final Pattern cookiePattern = Pattern.compile("([^=]+)=([^\\;]*);?\\s?");

  @Override
  public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
    Map<String, List<String>> requestHeaders = request.getHeaders();
    List<String> cookieHeaders = requestHeaders.get("cookie");
    String sessionId = null;
    if (cookieHeaders != null && cookieHeaders.size() > 0) {
      for (String cookieHeader : cookieHeaders) {
        Matcher matcher = cookiePattern.matcher(cookieHeader);
        while (matcher.find()) {
          String cookieKey = matcher.group(1);
          String cookieValue = matcher.group(2);
          if (cookieKey.equals("CRASHID")) {
            sessionId = cookieValue;
          }
        }
      }
    }
    if (sessionId == null) {
      sessionId = UUID.randomUUID().toString();
      response.getHeaders().put("Set-Cookie", Collections.singletonList("CRASHID=" + sessionId + "; Path=/"));
    } else {
    }
    sec.getUserProperties().put("CRASHID", sessionId);
  }
}
