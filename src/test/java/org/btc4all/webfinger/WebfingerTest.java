package org.btc4all.webfinger;

import org.apache.http.client.methods.HttpUriRequest;
import org.btc4all.webfinger.helpers.Response;
import org.btc4all.webfinger.pojo.JsonResourceDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InOrder;

import java.io.IOException;

import static org.btc4all.webfinger.matchers.Matchers.hasHostnameMatching;
import static org.btc4all.webfinger.matchers.Matchers.hasUrl;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

/**
 * @author Kosta Korenkov <7r0ggy@gmail.com>
 */
@RunWith(JUnit4.class)
public class WebFingerTest extends AbstractWebfingerClientTest {

    /**  RFC 3986 3.2.2 */
    @Test
    public void shouldConvertHostToLowercase() throws IOException, WebFingerClientException {
        setUpToRespondWith("valid_jrd.json");
        client.webFinger("bob@EXAMPLE.com");
        verifyHttpClientExecutedWithArgThat(hasHostnameMatching("example.com"));
    }

    /**  RFC 7033 4.2 */
    @Test (expected = WebFingerClientException.class)
    public void shouldFailOnInvalidCertResponse() throws WebFingerClientException {
        client.setHttpClient(testHelper.createTrustNoOneHttpClient().build());
        client.webFinger("paulej@packetizer.com");
    }

    /**  RFC 7033 4.2 */
    @Test (expected = WebFingerClientException.class)
    public void shouldFailWhenSecureConnectionIsNotEstablished() throws WebFingerClientException {
        client.setHttpClient(testHelper.createTrustNoOneHttpClient().build());
        // here we use the host that breaks up SSL connection
        client.webFinger("brett@onebigfluke.com");
    }

    /**  RFC 7033 4.2 */
    @Test
    public void shouldRedirectOnlyToHttpsURI() throws IOException, WebFingerClientException {
        setUpToRespondWithRedirectToValidResource(Response.found(), "http://example.org/bobs-data");

        JsonResourceDescriptor jrd = client.webFinger("bob@example.com");

        InOrder inOrder = inOrder(mockHttpClient);
        inOrder.verify(mockHttpClient, times(1)).execute(argThat(hasUrl("https://example.com/")));
        inOrder.verify(mockHttpClient, never()).execute(any(HttpUriRequest.class));
        assertNull(jrd);
    }

}
