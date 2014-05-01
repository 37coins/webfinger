package org.btc4all.webfinger;

import org.btc4all.webfinger.webfist.DKIMProofValidator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;

import static org.btc4all.webfinger.matchers.Matchers.hasHostnameMatching;
import static org.btc4all.webfinger.matchers.Matchers.hasParameterMatching;

/**
 * @author Kosta Korenkov <7r0ggy@gmail.com>
 */
@RunWith(JUnit4.class)
public class WebfingerTest extends AbstractWebfingerClientTest {

    /**  RFC 3986 3.2.2 */
    @Test
    public void shouldConvertHostToLowercase() throws IOException, WebfingerClientException {
        setUpToRespondWith("valid_jrd.json");
        client.webFinger("bob@EXAMPLE.com");
        verifyHttpClientExecutedWithArgThat(hasHostnameMatching("example.com"));
    }

    /**  RFC 7033 4.2 */
    @Test (expected = WebfingerClientException.class)
    public void shouldFailOnInvalidCertResponse() throws WebfingerClientException {
        client = new WebfingerClient(false, testHelper.createTrustNoOneHttpClient().build(), new DKIMProofValidator());
        client.webFinger("paulej@packetizer.com");
    }

    /**  RFC 7033 4.2 */
    @Test (expected = WebfingerClientException.class)
    public void shouldFailWhenSecureConnectionIsNotEstablished() throws WebfingerClientException {
        client = new WebfingerClient(false, testHelper.createTrustNoOneHttpClient().build(), new DKIMProofValidator());
        // here we use the host that breaks up SSL connection
        client.webFinger("brett@onebigfluke.com");
    }

    @Test
    public void shouldWorkForHttpResources() throws WebfingerClientException {
        setUpToRespondWith("valid_jrd.json");

        client.webFinger("http://example.com/bob");
        verifyHttpClientExecutedWithArgThat(hasParameterMatching("resource", "http%3A%2F%2Fexample\\.com%2Fbob"));
    }


}
