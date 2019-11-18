package org.crsh.command;

public class ShellSafety {
    private boolean safeShell = true;
    private boolean standAlone = false;
    private boolean internal = false;
    private boolean sshMode = false;
    private boolean allowExitInSafeMode = false;

    public ShellSafety() {
    }

    public ShellSafety(String safetyMode) {
        safeShell = safetyMode.contains("SAFESAFE");
        standAlone = safetyMode.contains("STANDALONE");
        internal = safetyMode.contains("INTERNAL");
        sshMode = safetyMode.contains("SSH");
        allowExitInSafeMode = safetyMode.contains("EXIT");
    }

    public String toSafeString() {
        String ret = "";
        if (safeShell) { ret += "|SAFESAFE"; }
        if (standAlone) { ret += "|STANDALONE"; }
        if (internal) { ret += "|INTERNAL"; }
        if (sshMode) { ret += "|SSH"; }
        if (allowExitInSafeMode) { ret += "|EXIT"; }
        return ret;
    }

    public String toString() {
        return toSafeString();
    }

    public boolean isSafeShell() {
        return safeShell;
    }

    public boolean isStandAlone() {
        return standAlone;
    }

    public boolean isInternal() {
        return internal;
    }

    public boolean isSshMode() {
        return sshMode;
    }

    public boolean isAllowExitInSafeMode() {
        return allowExitInSafeMode;
    }

    public void setSafeShell(boolean safeShell) {
        this.safeShell = safeShell;
    }

    public void setStandAlone(boolean standAlone) {
        this.standAlone = standAlone;
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }

    public void setSSH(boolean sshMode) {
        this.sshMode = sshMode;
    }

    public void setAllowExitInSafeMode(boolean exit) {
        this.allowExitInSafeMode = exit;
    }

    public boolean permitExit() {
        return !isSafeShell() || !isInternal() || isSshMode() || isAllowExitInSafeMode();
    }
};
