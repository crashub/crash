# Crash Application for Mule Standalone

## Installation

Drop the `crsh-x.y.z-mule-app.zip` distribution right into your Mule standalone apps directory, ie `$MULE_HOME/apps`.

To override the default ports create a file named `crash-config-override.properties` and drop it in `$MULE_HOME/conf`.

Default properties are:

    crash.auth=simple
    crash.auth.simple.username=root
    crash.auth.simple.password=mule
    crash.telnet.port=4020
    crash.ssh.port=4022
    crash.telnet.port=4020
    crash.ssh.port=4022


## Connection

With the default configuration, connect to the shell with either:

    telnet localhost 4020

or:

    ssh -p 4022 -l root localhost

using `mule` as password.


## Commands

Run the `mule` command to get the list of supported commands.
