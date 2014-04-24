package org.btc4all.webfinger;

import org.apache.http.StatusLine;
import org.apache.http.message.BasicStatusLine;
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
public class WebfingerClientRedirectTest extends AbstractWebfingerClientTest {

    private StatusLine statusLine;

    public WebfingerClientRedirectTest(StatusLine statusLine) {
        this.statusLine = statusLine;
    }

    @Parameterized.Parameters
    public static java.util.Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {new BasicStatusLine(Response.HTTP, 300, "Multiple Choices")},
                {new BasicStatusLine(Response.HTTP, 301, "Moved Permanently")},
                {new BasicStatusLine(Response.HTTP, 302, "See Other")},
                {new BasicStatusLine(Response.HTTP, 303, "See Other")},
                {new BasicStatusLine(Response.HTTP, 304, "Not Modified")},
                {new BasicStatusLine(Response.HTTP, 305, "Use Proxy")},
                {new BasicStatusLine(Response.HTTP, 306, "Switch Proxy")},
                {new BasicStatusLine(Response.HTTP, 307, "Temporary Redirect")},
                {new BasicStatusLine(Response.HTTP, 308, "Permanent Redirect")}
        });
    }

    /**
     * RFC 7033 4.2
     */
    @Test
    public void shouldWorkWithRedirectResponse() throws IOException {
        setUpToRespondWithRedirectToValidResource(statusLine, "https://example.org/bobs-data");

        JsonResourceDescriptor jrd = client.webFinger("bob@example.com");

        InOrder inOrder = inOrder(mockHttpClient);
        inOrder.verify(mockHttpClient, times(1)).execute(argThat(hasUrl("https://example.com/")));
        inOrder.verify(mockHttpClient, times(1)).execute(argThat(hasUrl("https://example.org/bobs-data")));
        assertNotNull(jrd);
    }

}
