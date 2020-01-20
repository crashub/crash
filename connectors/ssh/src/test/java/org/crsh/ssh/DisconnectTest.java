package org.crsh.ssh;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.session.ClientSession;
import org.crsh.auth.AuthInfo;
import org.crsh.auth.AuthenticationPlugin;
import org.crsh.auth.SimpleAuthenticationPlugin;
import org.crsh.plugin.CRaSHPlugin;
import org.crsh.shell.impl.command.CRaSHShellFactory;
import org.crsh.auth.DisconnectPlugin;
import org.crsh.ssh.term.inline.SSHInlinePlugin;
import org.crsh.util.Utils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import test.plugin.TestPluginLifeCycle;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class DisconnectTest {
    private TestPluginLifeCycle lifeCycle;

    private CountDownLatch disconnectLatch = new CountDownLatch(1);

    private int port;

    private static final AtomicInteger PORTS = new AtomicInteger(2000);

    public static class TestDisconnectHandler extends CRaSHPlugin<DisconnectPlugin> implements DisconnectPlugin {
        private final CountDownLatch latch;

        public TestDisconnectHandler(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onDisconnect(String userName, AuthInfo authInfo) {
            latch.countDown();
        }

        @Override
        public DisconnectPlugin getImplementation() {
            return this;
        }
    }
    @Before
    public void setUp() throws Exception {
        port = PORTS.getAndIncrement();
        SimpleAuthenticationPlugin auth = new SimpleAuthenticationPlugin();
        lifeCycle = new TestPluginLifeCycle(new SSHPlugin(), auth, new CRaSHShellFactory(), new SSHInlinePlugin(), new TestDisconnectHandler(disconnectLatch));
        lifeCycle.setProperty(SSHPlugin.SSH_PORT, port);
        lifeCycle.setProperty(SSHPlugin.SSH_ENCODING, Utils.UTF_8);
        lifeCycle.setProperty(AuthenticationPlugin.AUTH, Arrays.asList(auth.getName()));
        lifeCycle.setProperty(SimpleAuthenticationPlugin.SIMPLE_USERNAME, "admin");
        lifeCycle.setProperty(SimpleAuthenticationPlugin.SIMPLE_PASSWORD, "admin");
        lifeCycle.start();
    }

    @After
    public final void tearDown() {
        lifeCycle.stop();
    }

    @Test
    public void testDisconnectOnExit() throws Exception {
        SSHClient client = new SSHClient(port).connect();
        client.write("exit\n").flush();
        disconnectLatch.await(4L, TimeUnit.SECONDS);
        client.close();
    }

    @Test
    public void testDisconnectOnEOT() throws Exception {
        SSHClient client = new SSHClient(port).connect();
        client.write("\004").flush();
        disconnectLatch.await(4L, TimeUnit.SECONDS);
        client.close();
    }

    @Test
    public void testDisconnectWithRemoteCommand() throws Exception {
        SshClient client = SshClient.setUpDefaultClient();
        client.start();

        ClientSession session = client.connect("admin", "localhost", port).verify(4L, TimeUnit.SECONDS).getSession();
        session.addPasswordIdentity("admin");
        session.auth().verify(4L, TimeUnit.SECONDS);

        session.executeRemoteCommand("help");
        disconnectLatch.await(4L, TimeUnit.SECONDS);

        session.close(false);
        client.stop();
    }
}
