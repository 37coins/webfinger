package org.btc4all.webfinger;

import org.apache.http.client.methods.HttpUriRequest;
import org.btc4all.webfinger.helpers.Response;
import org.btc4all.webfinger.pojo.JsonResourceDescriptor;
import org.junit.Test;
import org.mockito.InOrder;

import java.io.IOException;

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
public class WebfingerHttpsTest extends AbstractWebfingerClientTest {

    /**  RFC 7033 4.2 */
    @Test
    public void shouldFailOnInvalidCertResponse() {
        client.setHttpClient(testHelper.createTrustNoOneHttpClient().build());
        JsonResourceDescriptor jrd = client.webFinger("paulej@packetizer.com");
        assertNull(jrd);
    }

    /**  RFC 7033 4.2 */
    @Test
    public void shouldFailWhenSecureConnectionIsNotEstablished() {
        client.setHttpClient(testHelper.createTrustNoOneHttpClient().build());
        // here we use the host that breaks up SSL connection
        JsonResourceDescriptor jrd = client.webFinger("brett@onebigfluke.com");
        assertNull(jrd);
    }

    /**  RFC 7033 4.2 */
    @Test
    public void shouldRedirectOnlyToHttpsURI() throws IOException {
        setUpToRespondWithRedirectToValidResource(Response.found(), "http://example.org/bobs-data");

        JsonResourceDescriptor jrd = client.webFinger("bob@example.com");

        InOrder inOrder = inOrder(mockHttpClient);
        inOrder.verify(mockHttpClient, times(1)).execute(argThat(hasUrl("https://example.com/")));
        inOrder.verify(mockHttpClient, never()).execute(any(HttpUriRequest.class));
        assertNull(jrd);
    }

}
