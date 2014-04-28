package org.btc4all.webfinger;

import org.btc4all.webfinger.helpers.Response;
import org.btc4all.webfinger.pojo.JsonResourceDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.InOrder;

import java.io.IOException;
import java.util.Arrays;

import static org.btc4all.webfinger.matchers.Matchers.hasUrl;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;

/**
 * @author Kosta Korenkov <7r0ggy@gmail.com>
 */
@RunWith(Parameterized.class)
public class WebfingerRedirectTest extends AbstractWebfingerClientTest {

    private int statusCode;
    
    private String reason;

    public WebfingerRedirectTest(int statusCode, String reason) {
        this.statusCode = statusCode;
        this.reason = reason;
    }

    @Parameterized.Parameters (name = "{0} {1}")
    public static java.util.Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                { 300, "Multiple Choices" },
                { 301, "Moved Permanently" },
                { 302, "See Other" },
                { 303, "See Other" },
                { 304, "Not Modified" },
                { 305, "Use Proxy" },
                { 306, "Switch Proxy" },
                { 307, "Temporary Redirect" },
                { 308, "Permanent Redirect" }
        });
    }

    /**
     * RFC 7033 4.2
     */
    @Test
    public void shouldWorkWithRedirectResponse() throws IOException {
        setUpToRespondWithRedirectToValidResource(Response.createResponse(statusCode, reason), "https://example.org/bobs-data");

        JsonResourceDescriptor jrd = client.webFinger("bob@example.com");

        InOrder inOrder = inOrder(mockHttpClient);
        inOrder.verify(mockHttpClient, times(1)).execute(argThat(hasUrl("https://example.com/")));
        inOrder.verify(mockHttpClient, times(1)).execute(argThat(hasUrl("https://example.org/bobs-data")));
        assertNotNull(jrd);
    }

}
