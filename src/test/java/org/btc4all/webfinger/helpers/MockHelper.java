package org.btc4all.webfinger.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

/**
 * @author Kosta Korenkov <7r0ggy@gmail.com>
 */
public class MockHelper {

    private ObjectMapper jsonMapper = new ObjectMapper();

    public MockData getData(String filename) {
        try {
            Map dataFixture = jsonMapper.readValue(new File("src/test/fixtures/" + filename), Map.class);

            return new MockData(String.valueOf(dataFixture.get("uri")), jsonMapper.writeValueAsString(dataFixture.get("response")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public HttpClientBuilder makeAllTrustingClient(HttpClientBuilder httpClientBuilder) {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            return httpClientBuilder
                    .setHostnameVerifier(SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)
                    .setSslcontext(sc);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    public HttpClientBuilder createTrustNoOneHttpClient() throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
        return HttpClients
                .custom()
                .setSslcontext(
                        SSLContexts
                                .custom()
                                .loadTrustMaterial(KeyStore.getInstance(KeyStore.getDefaultType()), new TrustStrategy() {
                                    public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                                        return false;
                                    }
                                })
                                .build()
                );
    }


}
