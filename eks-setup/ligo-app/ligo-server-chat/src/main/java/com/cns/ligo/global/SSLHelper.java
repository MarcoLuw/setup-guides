package com.cns.ligo.global;

import javax.net.ssl.*;
import java.security.cert.X509Certificate;
import java.security.NoSuchAlgorithmException;
import java.security.KeyManagementException;

// TODO : remove in production
public class SSLHelper {

  public static void disableSslVerification() throws NoSuchAlgorithmException, KeyManagementException {
    TrustManager[] trustAllCertificates = new TrustManager[]{
        new X509TrustManager() {
          public X509Certificate[] getAcceptedIssuers() {
            return null;
          }
          public void checkClientTrusted(X509Certificate[] certs, String authType) {}
          public void checkServerTrusted(X509Certificate[] certs, String authType) {}
        }
    };

    SSLContext sc = SSLContext.getInstance("TLS");
    sc.init(null, trustAllCertificates, new java.security.SecureRandom());
    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    HostnameVerifier allHostsValid = (hostname, session) -> true;
    HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
  }
}

