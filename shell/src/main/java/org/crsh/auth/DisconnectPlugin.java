package org.crsh.auth;

/**
 * Plugin for SSH session disconnect handling.
 */
public interface DisconnectPlugin {
    void onDisconnect(String userName, AuthInfo authInfo);
}
