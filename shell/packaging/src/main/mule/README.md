# Crash Application for Mule Standalone

## Installation

Drop the `crsh-x.y.z-mule-app.zip` distribution right into your Mule standalone apps directory, ie `$MULE_HOME/apps`.

To override the default ports create a file named `crash-config-override.properties` and drop it in `$MULE_HOME/conf`.

Default properties are:

    crash.telnet.port=4020
    crash.ssh.port=4022

## Commands

Get information about the broker:

    mule info

List all the deployed applications:

    mule apps
