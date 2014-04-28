package org.btc4all.webfinger;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.btc4all.webfinger.helpers.MockHelper;
import org.btc4all.webfinger.helpers.Response;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.IOException;

import static org.btc4all.webfinger.matchers.Matchers.hasUrl;
import static org.btc4all.webfinger.matchers.Matchers.isNot;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.*;

/**
 * @author Kosta Korenkov <7r0ggy@gmail.com>
 */
public class AbstractWebfingerClientTest {

    public static final String TEST_ACCT = "bob@example.com";

    protected static WebfingerClient client;

    protected static MockHelper testHelper;

    protected static HttpClient mockHttpClient;


    @BeforeClass
    public static void setUpClass() {
        testHelper = new MockHelper();
        mockHttpClient = mock(HttpClient.class);
        client = new WebfingerClient(false);
    }

    protected void setUpToRespondWith(String filename) {
        try {
            when(mockHttpClient.execute(any(HttpUriRequest.class))).thenReturn(
                    Response.OKResponseWithDataFromFile(filename)
            );
            client.setHttpClient(mockHttpClient);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void setUpToRespondWith(HttpResponse response) {
        try {
            when(mockHttpClient.execute(any(HttpUriRequest.class))).thenReturn(response);

            client.setHttpClient(mockHttpClient);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void setUpToRespondWithRedirectToValidResource(HttpResponse redirectResponse, final String location) {
        try {
            redirectResponse.setHeader("Location", location);

            when(mockHttpClient.execute(argThat(
                    isNot(hasUrl(location))
            ))).thenReturn(
                    redirectResponse
            );

            when(mockHttpClient.execute(argThat(
                    hasUrl(location)
            ))).thenReturn(
                    Response.OKResponseWithDataFromFile("valid_jrd.json")
            );

            client.setHttpClient(mockHttpClient);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Before
    public void setUp() throws Exception {
        reset(mockHttpClient);
        client.setHttpClient(HttpClientBuilder.create().build());
    }
}
