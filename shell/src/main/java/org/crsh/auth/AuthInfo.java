package org.crsh.auth;

public interface AuthInfo {

    boolean isSuccessful();

    AuthInfo UNSUCCESSFUL = new AuthInfo() {
        @Override
        public boolean isSuccessful() {
            return false;
        }
    };

    AuthInfo SUCCESSFUL = new AuthInfo() {
        @Override
        public boolean isSuccessful() {
            return true;
        }
    };
}
