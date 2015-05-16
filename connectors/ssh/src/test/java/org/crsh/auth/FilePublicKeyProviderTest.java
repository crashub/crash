package org.crsh.auth;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

public class FilePublicKeyProviderTest {

  @Test
  public void test() {
    String pubKeyFile = Thread.currentThread().getContextClassLoader().getResource("test_authorized_key.pem").getFile();
    assertTrue(new File(pubKeyFile).exists());
    FilePublicKeyProvider SUT = new FilePublicKeyProvider(new String[]{pubKeyFile});
    assertTrue(SUT.loadKeys().iterator().hasNext());
  }

}
