package org.crsh.command;

public class ShellSafety {
    private boolean safeShell = true;
    private boolean standAlone = false;
    private boolean internal = false;
    private boolean sshMode = false;

    public ShellSafety() {
    }

    public ShellSafety(String safetyMode) {
        safeShell = safetyMode.contains("SAFESAFE");
        standAlone = safetyMode.contains("STANDALONE");
        internal = safetyMode.contains("INTERNAL");
        sshMode = safetyMode.contains("SSH");
    }

    public String toSafeString() {
        String ret = "";
        if (safeShell) { ret += "|SAFESAFE"; }
        if (standAlone) { ret += "|STANDALONE"; }
        if (internal) { ret += "|INTERNAL"; }
        if (sshMode) { ret += "|SSH"; }
        return ret;
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
};
