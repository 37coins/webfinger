package org.btc4all.webfinger;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.btc4all.webfinger.helpers.Response;
import org.btc4all.webfinger.pojo.JsonResourceDescriptor;
import org.hamcrest.Matcher;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InOrder;

import java.io.IOException;

import static org.btc4all.webfinger.matchers.Matchers.hasUrl;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * Inherits the set of tests defined in WebFingerBasicTest to be ran against the WebFist fallback code.
 *
 * @author Kosta Korenkov <7r0ggy@gmail.com>
 */
@RunWith(JUnit4.class)
public class WebFistTest extends WebFingerBasicTest {

    @Test
    public void shouldFallbackToWebFist() throws IOException, WebFingerClientException {
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
    public void shouldFailIfWebFistServerIsUnavailable() throws IOException, WebFingerClientException {
        when(mockHttpClient.execute(any(HttpUriRequest.class)))
                .thenReturn(Response.notFound());
        when(mockHttpClient.execute(argThat(hasUrl("https://webfist.org/"))))
                .thenReturn(Response.notFound());

        try {
            client.webFinger("pithy.example@gmail.com");
            fail();
        } catch (WebFingerClientException e) {
            assertEquals(ResourceNotFoundException.class, e.getClass());
        }
    }

    @Test
    public void shouldFailIfDKIMSignatureIsInvalid() throws IOException, WebFingerClientException {
        when(mockHttpClient.execute(any(HttpUriRequest.class)))
                .thenReturn(Response.notFound());
        when(mockHttpClient.execute(argThat(hasUrl("https://webfist.org/"))))
                .thenReturn(Response.OKResponseWithDataFromFile("webfist_invalid_DKIM_response.json"));
        when(mockHttpClient.execute(argThat(
                hasUrl("http://webfist.org/webfist/proof/08e01fb3123de74555528daaeb2d33b513f50f88-c255b91b02617c067df89a3809f0e17197b52413?decrypt=pithy.example%40gmail.com")
        )))
            .thenReturn(Response.OKResponseWithDataFromFile("delegation_email_w_invalid_DKIM.eml"));

        JsonResourceDescriptor jrd = client.webFinger("pithy.example@gmail.com");

        verify(mockHttpClient, never()).execute(argThat(hasUrl("http://example.org/my-delegation-here.json")));
        assertNull(jrd);
    }

    @Test
    public void shouldValidateDKIMSignature() throws IOException, WebFingerClientException {
        when(mockHttpClient.execute(any(HttpUriRequest.class)))
                .thenReturn(Response.notFound());
        when(mockHttpClient.execute(argThat(hasUrl("https://webfist.org/"))))
                .thenReturn(Response.OKResponseWithDataFromFile("webfist_response.json"));

        JsonResourceDescriptor jrd = client.webFinger("pithy.example@gmail.com");

        InOrder inOrder = inOrder(mockHttpClient);
        inOrder.verify(mockHttpClient, times(1)).execute(argThat(hasUrl("https://gmail.com/")));
        inOrder.verify(mockHttpClient, times(1)).execute(argThat(hasUrl("https://webfist.org/")));
        inOrder.verify(mockHttpClient, times(1)).execute(argThat(
                hasUrl("http://webfist.org/webfist/proof/08e01fb3123de74555528daaeb2d33b513f50f88-c255b91b02617c067df89a3809f0e17197b52413?decrypt=pithy.example%40gmail.com")
        ));
        assertNotNull(jrd);
    }



    @BeforeClass
    public static void setUpWebFistTestClass() throws IOException {
        client = new WebFingerClient(true);
        client.setHttpClient(mockHttpClient);
    }

    @Override
    protected void setUpToRespondWith(String filename) {
        try {
            when(mockHttpClient.execute(any(HttpUriRequest.class)))
                    .thenReturn(Response.notFound());
            when(mockHttpClient.execute(argThat(hasUrl("https://webfist.org/"))))
                    .thenReturn(Response.OKResponseWithDataFromFile("webfist_response.json"));
            when(mockHttpClient.execute(argThat(hasUrl("http://example.org/my-delegation-here.json"))))
                    .thenReturn(Response.OKResponseWithDataFromFile(filename));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

}
