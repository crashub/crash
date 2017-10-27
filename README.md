# Corda Shell

This is a patch set on top of the excellent but unmaintained CRaSH project which you can find at http://www.crashub.org.

It consists of the last release of CRaSH, with various pull requests merged and our own patches added. A brief summary of the changes are:
 
 * Upgraded Apache SSHD to version 1.6.0, bumped required Java version to 7. Also re-enabled reading/generating PEM host key files on startup
 * Thanks to Marek Skocovsky, Apache SSHD has been upgraded to version 1.3.0 which resolves bugs related to evolution of the SSH protocol over the years.
 * Thanks to David Ribyrne, A new ExternalResolver class  which lets you add pre-compiled command classes.
 
Future planned changes include:

 * Disabling commands that have bitrotted
 * Commands to control/access log4j

Please note that this is not a 'true' fork - the software is drop-in compatible and still calls itself CRaSH internally.
We are not uploading it to Maven Central, so you can depend on it using jitpack.io instead. There are tags named A1, A2 etc
that represent somewhat tested snapshots.
