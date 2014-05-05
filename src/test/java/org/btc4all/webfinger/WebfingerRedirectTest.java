package org.btc4all.webfinger;

import org.apache.commons.io.IOUtils;
import org.btc4all.webfinger.helpers.MockHelper;
import org.btc4all.webfinger.pojo.JsonResourceDescriptor;
import org.btc4all.webfinger.webfist.DKIMProofValidator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.model.Header;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.*;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

/**
 * RFC 7033 4.2
 *
 * @author Kosta Korenkov <7r0ggy@gmail.com>
 */
@RunWith(Parameterized.class)
public class WebfingerRedirectTest {

    protected static WebfingerClient client;

    protected static MockServerClient mockServer;

    private int statusCode;

    private String location;

    private boolean expectToSucceed;

    public WebfingerRedirectTest(int statusCode, String location, boolean expectToSucceed) {
        this.statusCode = statusCode;
        this.location = location;
        this.expectToSucceed = expectToSucceed;
    }

    @BeforeClass
    public static void setUpClass() {
        client = new WebfingerClient(false,
                new MockHelper().makeAllTrustingClient(HttpClientFactory.getClientBuilder()).build(),
                new DKIMProofValidator());

        mockServer = new MockServerClient("localhost", 1080);
    }

    @Parameterized.Parameters(name = "{0} {1}. Allowed: {2}")
    public static java.util.Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {301, "https://127.0.0.1:1082/bobs-data", true},
                {302, "https://127.0.0.1:1082/bobs-data", true},
                {303, "https://127.0.0.1:1082/bobs-data", true},
                {307, "https://127.0.0.1:1082/bobs-data", true},
                {308, "https://127.0.0.1:1082/bobs-data", true},
                {301, "http://127.0.0.1:1080/bobs-data", false}
        });
    }

    @Before
    public void setUpMockResponses() throws IOException {
        mockServer.reset();
        mockServer
                .when(
                        request().withURL(location)
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(IOUtils.toString(MockHelper.getFixtureInputStream("valid_jrd.json")))
                );
        mockServer
                .when(
                        request()
                )
                .respond(
                        response()
                                .withStatusCode(statusCode)
                                .withHeader(new Header("Location", location))
                );
    }

    /**
     * RFC 7033 4.2
     */
    @Test
    public void shouldWorkWithRedirectResponse() throws WebfingerClientException {
        try {
            JsonResourceDescriptor jrd = client.webFinger("bob@127.0.0.1:1082");

            assertNotNull(jrd);
            assertEquals(jrd.getSubject().toString(), "acct:bob@example.com");
        } catch (WebfingerClientException e) {
            assertFalse(expectToSucceed);
        }
    }

}
