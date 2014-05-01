package org.btc4all.webfinger;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.btc4all.webfinger.helpers.Response;
import org.btc4all.webfinger.pojo.JsonResourceDescriptor;
import org.btc4all.webfinger.webfist.ProofValidationException;
import org.btc4all.webfinger.webfist.ProofValidator;
import org.hamcrest.Matcher;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InOrder;

import java.io.IOException;

import static org.btc4all.webfinger.matchers.Matchers.hasUrl;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.*;

/**
 * Inherits the set of tests defined in WebfingerBasicTest to be ran against the WebFist fallback code.
 *
 * @author Kosta Korenkov <7r0ggy@gmail.com>
 */
@RunWith(JUnit4.class)
public class WebfistTest extends WebfingerBasicTest {

    @Test
    public void shouldFallbackToWebFist() throws IOException, WebfingerClientException {
        setUpToRespondWith("valid_jrd.json");

        JsonResourceDescriptor jrd = client.webFinger("pithy.example@gmail.com");

        InOrder inOrder = inOrder(mockHttpClient);
        inOrder.verify(mockHttpClient, times(1)).execute(argThat(hasUrl("https://gmail.com/")));
        inOrder.verify(mockHttpClient, times(1)).execute(argThat(
                hasUrl("https://webfist.org/.well-known/webfinger?resource=acct%3Apithy.example%40gmail.com")
        ));
        inOrder.verify(mockHttpClient, times(1)).execute(argThat(
                hasUrl("http://example.org/my-delegation-here.json")
        ));
        assertNotNull(jrd);
    }

    @Test
    public void shouldFallbackToWebFistOnlyForAcctResources() throws IOException, WebfingerClientException {
        setUpToRespondWith("valid_jrd.json");

        try {
            client.webFinger("http://example.com/bob");
            fail("Expected WebfingerClientException");
        } catch (WebfingerClientException e) {
            assertEquals(ResourceNotFoundException.class, e.getClass());
        }

        InOrder inOrder = inOrder(mockHttpClient);
        inOrder.verify(mockHttpClient, times(1)).execute(argThat(hasUrl("https://example.com/")));
        inOrder.verify(mockHttpClient, never()).execute(any(HttpUriRequest.class));
    }


    @Test
    public void shouldFailIfWebFistServerIsUnavailable() throws IOException, WebfingerClientException {
        when(mockHttpClient.execute(any(HttpUriRequest.class)))
                .thenReturn(Response.notFound());
        when(mockHttpClient.execute(argThat(hasUrl("https://webfist.org/"))))
                .thenReturn(Response.notFound());

        try {
            client.webFinger("pithy.example@gmail.com");
            fail("Call to the client should fail");
        } catch (WebfingerClientException e) {
            assertEquals(ResourceNotFoundException.class, e.getClass());
        }
    }


    @BeforeClass
    public static void setUpWebFistTestClass() throws IOException {
        client = new WebfingerClient(true, mockHttpClient, new NopProofValidator());
    }

    @Override
    protected void setUpToRespondWith(String filename) {
        setUpToRespondWith(Response.OKResponseWithDataFromFile(filename));
    }

    @Override
    protected void setUpToRespondWith(HttpResponse response) {
        try {
            when(mockHttpClient.execute(any(HttpUriRequest.class)))
                    .thenReturn(Response.notFound());
            when(mockHttpClient.execute(argThat(hasUrl("https://webfist.org/"))))
                    .thenReturn(Response.OKResponseWithDataFromFile("webfist_response.json"));
            when(mockHttpClient.execute(argThat(hasUrl("http://example.org/my-delegation-here.json"))))
                    .thenReturn(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void verifyHttpClientExecutedWithArgThat(Matcher<HttpUriRequest> matcher) {
        try {
            InOrder inOrder = inOrder(mockHttpClient);
            inOrder.verify(mockHttpClient, times(1)).execute(argThat(hasUrl("https://example.com/")));
            inOrder.verify(mockHttpClient).execute(argThat(matcher));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static class NopProofValidator implements ProofValidator {
        @Override
        public void validate(String resource, String proofLink) throws ProofValidationException {
        }
    }

}
