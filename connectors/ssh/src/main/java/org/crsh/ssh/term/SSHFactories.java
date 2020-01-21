package org.crsh.ssh.term;

import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.cipher.BuiltinCiphers;
import org.apache.sshd.common.cipher.Cipher;
import org.apache.sshd.common.compression.BuiltinCompressions;
import org.apache.sshd.common.compression.Compression;
import org.apache.sshd.common.compression.CompressionFactory;
import org.apache.sshd.common.kex.BuiltinDHFactories;
import org.apache.sshd.common.kex.KeyExchange;
import org.apache.sshd.common.mac.BuiltinMacs;
import org.apache.sshd.common.mac.Mac;
import org.apache.sshd.common.signature.BuiltinSignatures;
import org.apache.sshd.common.signature.Signature;
import org.apache.sshd.server.ServerBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * ENT-4682: List of supported SSH algorithms and ciphers according to modern security requirements.
 * Outdated MD5, SHA-1, CBC, 3-DES, RC4 and Blowfish are excluded.
 */
public class SSHFactories {
    private static final List<BuiltinSignatures> SIGNATURE_PREFERENCE =
            Collections.unmodifiableList(
                    Arrays.asList(
                            BuiltinSignatures.nistp256,
                            BuiltinSignatures.nistp384,
                            BuiltinSignatures.nistp521,
                            BuiltinSignatures.ed25519,
                            BuiltinSignatures.rsaSHA512,
                            BuiltinSignatures.rsaSHA256,
                            BuiltinSignatures.rsa
                    ));

    private static final List<BuiltinCiphers> CIPHER_PREFERENCE =
            Collections.unmodifiableList(
                    Arrays.asList(
                            BuiltinCiphers.aes128ctr,
                            BuiltinCiphers.aes192ctr,
                            BuiltinCiphers.aes256ctr
                    ));

    private static final List<BuiltinDHFactories> KEX_PREFERENCE =
            Collections.unmodifiableList(
                    Arrays.asList(
                            BuiltinDHFactories.ecdhp521,
                            BuiltinDHFactories.ecdhp384,
                            BuiltinDHFactories.ecdhp256,
                            BuiltinDHFactories.dhg14_256,
                            BuiltinDHFactories.dhg16_512,
                            BuiltinDHFactories.dhg18_512
                    ));

    private static final List<BuiltinMacs> MAC_PREFERENCE =
            Collections.unmodifiableList(
                    Arrays.asList(
                            BuiltinMacs.hmacsha256,
                            BuiltinMacs.hmacsha512
                    ));

    private static final List<CompressionFactory> COMPRESSION_PREFERENCE =
            Collections.unmodifiableList(
                    Arrays.asList(
                            BuiltinCompressions.none
                    ));

    public static List<NamedFactory<Signature>> setUpSignatureFactories() {
        return NamedFactory.setUpBuiltinFactories(false, SIGNATURE_PREFERENCE);
    }

    public static List<NamedFactory<Cipher>> setUpCipherFactories() {
        return NamedFactory.setUpBuiltinFactories(false, CIPHER_PREFERENCE);
    }

    public static List<NamedFactory<KeyExchange>> setUpKeyExchangeFactories() {
        return NamedFactory.setUpTransformedFactories(false, KEX_PREFERENCE, ServerBuilder.DH2KEX);
    }

    public static List<NamedFactory<Mac>> setUpMacFactories() {
        return NamedFactory.setUpBuiltinFactories(false, MAC_PREFERENCE);
    }

    public static List<NamedFactory<Compression>> setUpCompressionFactories() {
        return NamedFactory.setUpBuiltinFactories(false, COMPRESSION_PREFERENCE);
    }
}
