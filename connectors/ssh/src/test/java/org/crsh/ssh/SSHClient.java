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
package org.crsh.ssh;

import org.apache.sshd.client.auth.password.PasswordIdentityProvider;
import org.apache.sshd.client.future.AuthFuture;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelShell;
import org.apache.sshd.common.channel.PtyMode;
import org.apache.sshd.common.channel.SttySupport;
import org.apache.sshd.common.future.SshFutureListener;
import org.crsh.ssh.term.SSHLifeCycle;
import org.crsh.util.Utils;

import java.io.*;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SSHClient {

  static final String TTY =
      "speed 9600 baud; 36 rows; 180 columns;\n" +
      "lflags: icanon isig iexten echo echoe -echok echoke -echonl echoctl\n" +
      "\t-echoprt -altwerase -noflsh -tostop -flusho pendin -nokerninfo\n" +
      "\t-extproc\n" +
      "iflags: -istrip icrnl -inlcr -igncr ixon -ixoff ixany imaxbel iutf8\n" +
      "\t-ignbrk brkint -inpck -ignpar -parmrk\n" +
      "oflags: opost onlcr -oxtabs -onocr -onlret\n" +
      "cflags: cread cs8 -parenb -parodd hupcl -clocal -cstopb -crtscts -dsrflow\n" +
      "\t-dtrflow -mdmbuf\n" +
      "cchars: discard = ^O; dsusp = ^Y; eof = ^D; eol = <undef>;\n" +
      "\teol2 = <undef>; erase = ^?; intr = ^C; kill = ^U; lnext = ^V;\n" +
      "\tmin = 1; quit = ^\\; reprint = ^R; start = ^Q; status = ^T;\n" +
      "\tstop = ^S; susp = ^Z; time = 0; werase = ^W;";

  /** . */
  private OutputStream out;

  /** . */
  private InputStream in;

  /** . */
  private SshClient client;

  /** . */
  private ClientSession session;

  /** . */
  private ChannelShell channel;

  /** . */
  private int port;

  public SSHClient() {
    this(2000);
  }

  public SSHClient(int port) {
    this.port = port;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  private final Logger log = Logger.getLogger(SSHClient.class.getName());

  public SSHClient connect() throws Exception {

    Map<PtyMode, Integer> tty = SttySupport.parsePtyModes(TTY);

    //
    SshClient client = SshClient.setUpDefaultClient();
    client.start();

    //
    ClientSession session = client.connect("admin", "localhost", port).verify(4L, TimeUnit.SECONDS).getSession();
    session.addPasswordIdentity("admin");
    AuthFuture authFuture = session.auth().verify(4L, TimeUnit.SECONDS);

    authFuture.addListener(new SshFutureListener<AuthFuture>() {
        @Override
        public void operationComplete(AuthFuture authFuture) {
          log.info("Authentication completed with " + (authFuture.isSuccess() ? "success" : "failure"));
        }
    });

    //
    ChannelShell channel = session.createShellChannel();
    channel.setPtyModes(tty);

    //
    PipedOutputStream out = new PipedOutputStream();
    PipedInputStream channelIn = new PipedInputStream(out);

    //
    PipedOutputStream channelOut = new PipedOutputStream();
    PipedInputStream in = new PipedInputStream(channelOut);

    //
    channel.setIn(channelIn);
    channel.setOut(channelOut);
    channel.setErr(new ByteArrayOutputStream());
    channel.open();

    //
    this.channel = channel;
    this.client = client;
    this.session = session;
    this.out = out;
    this.in = in;

    //
    return this;
  }

  public SSHClient write(CharSequence s) throws IOException {
    return write(s.toString().getBytes("UTF-8"));
  }

  public int read() throws IOException {
    return in.read();
  }

  public SSHClient write(byte... bytes) throws IOException {
    out.write(bytes);
    return this;
  }

  public SSHClient flush() throws IOException {
    out.flush();
    return this;
  }

  public SSHClient close() {
    try {
      Utils.close(out);
      channel.close(false);
      session.close(false);
      client.stop();
      return this;
    } finally {
      this.client = null;
      this.channel = null;
      this.session = null;
      this.out = null;
    }
  }
}
