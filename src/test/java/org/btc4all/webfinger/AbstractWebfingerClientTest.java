package org.btc4all.webfinger;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.btc4all.webfinger.helpers.MockHelper;
import org.btc4all.webfinger.helpers.Response;
import org.btc4all.webfinger.webfist.DKIMProofValidator;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.IOException;

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
        client = new WebfingerClient(false, mockHttpClient, new DKIMProofValidator());
    }

    @Before
    public void setUp() throws Exception {
        reset(mockHttpClient);
    }

    protected void setUpToRespondWith(String filename) {
        try {
            when(mockHttpClient.execute(any(HttpUriRequest.class))).thenReturn(
                    Response.OKResponseWithDataFromFile(filename)
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void setUpToRespondWith(HttpResponse response) {
        try {
            when(mockHttpClient.execute(any(HttpUriRequest.class))).thenReturn(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void verifyHttpClientExecutedWithArgThat(Matcher<HttpUriRequest> matcher) {
        try {
            verify(mockHttpClient).execute(argThat(matcher));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
