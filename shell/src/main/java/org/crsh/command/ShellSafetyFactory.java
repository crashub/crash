package org.crsh.command;


import java.util.HashMap;

public class ShellSafetyFactory {
    static private HashMap<Long, ShellSafety> safetyByThread = new HashMap<Long, ShellSafety>();
    static public ShellSafety getCurrentThreadShellSafety() {
        long threadId = Thread.currentThread().getId();
        synchronized (safetyByThread) {
            if (safetyByThread.containsKey(threadId)) {
                return safetyByThread.get(threadId);
            }
        }

        ShellSafety ret = new ShellSafety();
        ret.setSafeShell(false);
        return ret;
    }

    static public void registerShellSafetyForThread(ShellSafety shellSafety) {
        long threadId = Thread.currentThread().getId();
        synchronized (safetyByThread) {
            safetyByThread.put(threadId, shellSafety);
        }
    }
}
