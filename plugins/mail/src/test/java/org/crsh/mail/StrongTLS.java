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
package org.crsh.mail;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class StrongTLS {

  /**
   * The protocols that are enabled.
   */
  public static final String[] ENABLED_PROTOCOLS = new String[] {

      // Strong protocols

      "SSLv3",
      "TLSv1",
      "TLSv1.1",
      "SSLv2Hello",

      // Weak protocols

//            "SSLv2"

  };

  /**
   * The SSL cipher suites that are enabled.
   */
  public static final String[] ENABLED_CIPHER_SUITES = new String[] {

      // Cipher suites that are not listed at
      // http://java.sun.com/javase/6/docs/technotes/guides/security/StandardNames.html
      // but are known to be strong.

      "TLS_RSA_WITH_DES_CBC_SHA",
      "TLS_DHE_DSS_WITH_DES_CBC_SHA",
      "TLS_DHE_RSA_WITH_DES_CBC_SHA",
      "TLS_DHE_DSS_EXPORT1024_WITH_DES_CBC_SHA",
      "TLS_RSA_WITH_3DES_EDE_CBC_SHA",
      "TLS_RSA_WITH_RC4_128_SHA",
      "TLS_RSA_WITH_RC4_128_MD5",
      "TLS_DHE_DSS_WITH_3DES_EDE_CBC_SHA",
      "TLS_DHE_DSS_WITH_RC4_128_SHA",
      "TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA",

      // Strong cipher suites that are listed at
      // http://java.sun.com/javase/6/docs/technotes/guides/security/StandardNames.html

      "SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA",
      "SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA",
      "TLS_RSA_WITH_AES_128_CBC_SHA",
      "TLS_RSA_WITH_AES_256_CBC_SHA",
      "SSL_RSA_WITH_3DES_EDE_CBC_SHA",
      "SSL_RSA_WITH_RC4_128_MD5",
      "SSL_RSA_WITH_RC4_128_SHA",

      // Cipher suites that are listed at
      // http://java.sun.com/javase/6/docs/technotes/guides/security/StandardNames.html
      // that are know to be weak, or are of unknown strength.

//            "SSL_DH_DSS_WITH_3DES_EDE_CBC_SHA",
//            "SSL_DH_DSS_WITH_DES_CBC_SHA",
//            "SSL_DH_DSS_EXPORT_WITH_DES40_CBC_SHA",
//            "SSL_DH_RSA_WITH_DES_CBC_SHA",
//            "SSL_DH_RSA_WITH_3DES_EDE_CBC_SHA",
//            "SSL_DH_RSA_EXPORT_WITH_DES40_CBC_SHA",
//            "TLS_DHE_DSS_WITH_AES_128_CBC_SHA",
//            "TLS_DHE_DSS_WITH_AES_256_CBC_SHA",
//            "SSL_DHE_DSS_WITH_DES_CBC_SHA",
//            "SSL_DHE_DSS_WITH_RC4_128_SHA",
//            "SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA",
//            "TLS_DHE_RSA_WITH_AES_128_CBC_SHA",
//            "TLS_DHE_RSA_WITH_AES_256_CBC_SHA",
//            "SSL_DHE_RSA_WITH_DES_CBC_SHA",
//            "SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA",
//            "SSL_DHE_DSS_EXPORT1024_WITH_DES_CBC_SHA",
//            "SSL_DHE_DSS_EXPORT1024_WITH_RC4_56_SHA",
//            "TLS_DH_anon_WITH_AES_128_CBC_SHA",
//            "TLS_DH_anon_WITH_AES_256_CBC_SHA",
//            "SSL_DH_anon_WITH_3DES_EDE_CBC_SHA",
//            "SSL_DH_anon_WITH_DES_CBC_SHA",
//            "SSL_DH_anon_WITH_RC4_128_MD5",
//            "SSL_DH_anon_EXPORT_WITH_DES40_CBC_SHA",
//            "SSL_DH_anon_EXPORT_WITH_RC4_40_MD5",
//            "SSL_FORTEZZA_DMS_WITH_NULL_SHA",
//            "SSL_FORTEZZA_DMS_WITH_FORTEZZA_CBC_SHA",
//            "SSL_RSA_WITH_DES_CBC_SHA",
//            "SSL_RSA_WITH_IDEA_CBC_SHA",
//            "SSL_RSA_WITH_NULL_MD5",
//            "SSL_RSA_WITH_NULL_SHA",
//            "SSL_RSA_EXPORT_WITH_RC2_CBC_40_MD5",
//            "SSL_RSA_EXPORT_WITH_RC4_40_MD5",
//            "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA",
//            "SSL_RSA_EXPORT1024_WITH_RC4_56_SHA",
//            "SSL_RSA_EXPORT1024_WITH_DES_CBC_SHA",
//            "SSL_RSA_FIPS_WITH_DES_CBC_SHA",
//            "SSL_RSA_FIPS_WITH_3DES_EDE_CBC_SHA",
//            "TLS_KRB5_WITH_3DES_EDE_CBC_MD5",
//            "TLS_KRB5_WITH_3DES_EDE_CBC_SHA",
//            "TLS_KRB5_WITH_DES_CBC_MD5",
//            "TLS_KRB5_WITH_DES_CBC_SHA",
//            "TLS_KRB5_WITH_IDEA_CBC_SHA",
//            "TLS_KRB5_WITH_IDEA_CBC_MD5",
//            "TLS_KRB5_WITH_RC4_128_MD5",
//            "TLS_KRB5_WITH_RC4_128_SHA",
//            "TLS_KRB5_EXPORT_WITH_DES_CBC_40_MD5",
//            "TLS_KRB5_EXPORT_WITH_DES_CBC_40_SHA",
//            "TLS_KRB5_EXPORT_WITH_RC2_CBC_40_SHA",
//            "TLS_KRB5_EXPORT_WITH_RC2_CBC_40_MD5",
//            "TLS_KRB5_EXPORT_WITH_RC4_40_MD5",
//            "TLS_KRB5_EXPORT_WITH_RC4_40_SHA",
//            "TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA",
//            "TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA",
//            "TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA",
//            "TLS_ECDH_ECDSA_WITH_RC4_128_SHA",
//            "TLS_ECDH_ECDSA_WITH_NULL_SHA",
//            "TLS_ECDH_RSA_WITH_AES_128_CBC_SHA",
//            "TLS_ECDH_RSA_WITH_AES_256_CBC_SHA",
//            "TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA",
//            "TLS_ECDH_RSA_WITH_RC4_128_SHA",
//            "TLS_ECDH_RSA_WITH_NULL_SHA",
//            "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA",
//            "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA",
//            "TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA",
//            "TLS_ECDHE_ECDSA_WITH_RC4_128_SHA",
//            "TLS_ECDHE_ECDSA_WITH_NULL_SHA",
//            "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
//            "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA",
//            "TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA",
//            "TLS_ECDHE_RSA_WITH_RC4_128_SHA",
//            "TLS_ECDHE_RSA_WITH_NULL_SHA",
//            "TLS_ECDH_anon_WITH_AES_128_CBC_SHA",
//            "TLS_ECDH_anon_WITH_AES_256_CBC_SHA",
//            "TLS_ECDH_anon_WITH_3DES_EDE_CBC_SHA",
//            "TLS_ECDH_anon_WITH_RC4_128_SHA",
//            "TLS_ECDH_anon_WITH_NULL_SHA",
  };

  /**
   * Gives the intersection of 2 string arrays.
   *
   * @param stringSetA a set of strings (not null)
   * @param stringSetB another set of strings (not null)
   * @return the intersection of strings in stringSetA and stringSetB
   */
  public static String[] intersection(String[] stringSetA, String[] stringSetB) {
    Set<String> intersection = new HashSet<String>(Arrays.asList(stringSetA));
    intersection.retainAll(Arrays.asList(stringSetB));
    return intersection.toArray(new String[intersection.size()]);
  }

}
