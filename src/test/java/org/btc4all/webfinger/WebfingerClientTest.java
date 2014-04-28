package org.btc4all.webfinger;

import org.apache.http.Header;
import org.apache.http.client.methods.HttpUriRequest;
import org.btc4all.webfinger.helpers.Response;
import org.btc4all.webfinger.pojo.JsonResourceDescriptor;
import org.btc4all.webfinger.pojo.Link;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InOrder;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;

import static org.btc4all.webfinger.matchers.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**  @author Kosta Korenkov <7r0ggy@gmail.com> */
@RunWith(JUnit4.class)
public class WebfingerClientTest extends AbstractWebfingerClientTest {


    /**  RFC 7033 4.4 */
    @Test
    public void shouldIgnoreUnknownJRDMembers() {
        setUpToRespondWith("unwanted_member_jrd.json");
        JsonResourceDescriptor jrd = client.webFinger(TEST_ACCT);
        assertNotNull(jrd);
    }

    /**  RFC 7033 4.4 */
    @Test
    public void shouldAcceptResponseWithMinimalJRD() {
        setUpToRespondWith("minimal_jrd.json");
        JsonResourceDescriptor jrd = client.webFinger(TEST_ACCT);
        assertNull(jrd.getAliases());
        assertNull(jrd.getProperties());
        assertNull(jrd.getLinks());
    }

    /**  RFC 7033 4.4 */
    @Test
    public void shouldAcceptResponseWithLinksWithoutTypeAndHref() {
        setUpToRespondWith("valid_jrd.json");
        JsonResourceDescriptor jrd = client.webFinger(TEST_ACCT);
        assertEquals("http://webfinger.example/rel/no-href-link", jrd.getLinks().get(1).getRel());
        assertNull(jrd.getLinks().get(1).getType());
        assertNull(jrd.getLinks().get(1).getHref());
    }

    /**  RFC 7033 4.4 */
    @Test
    public void shouldReturnAllKnownJRDMembers() {
        setUpToRespondWith("full_jrd.json");
        JsonResourceDescriptor jrd = client.webFinger("bob@example.com");

        assertEquals("acct:bob@example.com", jrd.getSubject().toString());

        // aliases
        assertEquals(1, jrd.getAliases().size());
        assertEquals("https://www.example.com/~bob/", jrd.getAliases().iterator().next().toString());

        // properties
        assertEquals(2, jrd.getProperties().size());
        Iterator properties = jrd.getProperties().entrySet().iterator();
        assertEquals("http://example.com/ns/role=employee", properties.next().toString());
        assertEquals("http://example.com/ns/sex=male", properties.next().toString());

        // links
        assertEquals(1, jrd.getLinks().size());
        Link link = jrd.getLinks().get(0);
        assertEquals("http://webfinger.example/rel/profile-page", link.getRel());
        assertEquals("text/html", link.getType());
        assertEquals("https://www.example.com/~bob/", link.getHref().toString());

        // link titles
        assertEquals(2, link.getTitles().size());
        Iterator titles = link.getTitles().entrySet().iterator();
        assertEquals("en-us=Bob's webpage", titles.next().toString());
        assertEquals("und=La pagina web de Bob", titles.next().toString());

        // link properties
        assertEquals(1, link.getProperties().size());
        assertEquals("http://webfinger.example/rel/nsfw=true", link.getProperties().entrySet().iterator().next().toString());
    }

    /**  RFC 7033 4.4.4.4
     * A JRD SHOULD NOT include more than one title identified with the same
     * language tag (or "und") within the link relation object.  Meaning is
     * undefined if a link relation object includes more than one title
     * named with the same language tag (or "und"), though this MUST NOT be
     * treated as an error.  A client MAY select whichever title or titles
     * it wishes to utilize.
     * */
    @Test
    public void shouldPreferTheLastLanguageTagTitleValueIfSeveralExistForTheSameTag() {
        setUpToRespondWith("duplicate_language_tag_titles_jrd.json");
        JsonResourceDescriptor jrd = client.webFinger(TEST_ACCT);
        Link link = jrd.getLinks().get(0);

        // link titles
        assertEquals(2, link.getTitles().size());
        Iterator titles = link.getTitles().entrySet().iterator();
        assertEquals("en-us=Bob's profile page", titles.next().toString());
        assertEquals("und=La pagina web de Bob", titles.next().toString());
    }

    /**  RFC 7033 4.2 */
    @Test
    public void shouldUseOnlyHttps() throws IOException {
        setUpToRespondWith("valid_jrd.json");
        client.webFinger(TEST_ACCT);
        verify(mockHttpClient).execute(argThat(hasHttpsScheme()));
    }

    /**  RFC 3986 3.2.2 */
    @Test
    public void shouldConvertHostToLowercase() throws IOException {
        setUpToRespondWith(Response.notFound());
        client.webFinger("bob@EXAMPLE.com");
        verify(mockHttpClient).execute(argThat(hasHostnameMatching("example.com")));
    }

    /**  RFC 7033 4.1 */
    @Test
    public void requestShouldContainResourceParameterInQuery() throws IOException {
        setUpToRespondWith("valid_jrd.json");
        client.webFinger(TEST_ACCT);
        verify(mockHttpClient).execute(argThat(hasParameterMatching("resource", ".*")));
    }

    /**  RFC 7033 8.1 */
    @Test
    public void requestShouldContainURIScheme() throws IOException {
        setUpToRespondWith("valid_jrd.json");
        client.webFinger("bob@example.com");
        verify(mockHttpClient).execute(argThat(hasParameterMatching("resource", "^\\w+%3A.*")));
    }

    @Test
    public void shouldWorkWithResourcesWithScheme() throws IOException {
        setUpToRespondWith("valid_jrd.json");
        client.webFinger("acct:bob@example.com");
        verify(mockHttpClient).execute(argThat(hasParameterMatching("resource", "acct%3Abob%40example\\.com")));

        client.webFinger("http://example.com/bob");
        verify(mockHttpClient).execute(argThat(hasParameterMatching("resource", "http%3A%2F%2Fexample\\.com%2Fbob")));
    }

    /**  RFC 7033 4.1 */
    @Test
    public void requestParametersShouldBePercentEncoded() throws IOException {
        setUpToRespondWith("valid_jrd.json");
        client.webFinger("bob@example.com");
        verify(mockHttpClient).execute(argThat(hasParameterMatching("resource", "acct%3Abob%40example\\.com")));
    }

    /**  RFC 7033 4.2 */
    @Test
    public void shouldFailOn4xxResponse() {
        setUpToRespondWith(Response.notFound());
        assertNull(client.webFinger(TEST_ACCT)); //TODO: is it really the best way to specify that no data found?

        setUpToRespondWith(Response.forbidden());
        assertNull(client.webFinger(TEST_ACCT));

        setUpToRespondWith(Response.badRequest());
        assertNull(client.webFinger(TEST_ACCT));
    }

    /**  RFC 7033 4.2 */
    @Test
    public void shouldFailOn5xxResponse() {
        setUpToRespondWith(Response.serverError());
        assertNull(client.webFinger(TEST_ACCT));

        setUpToRespondWith(Response.serviceUnavailable());
        assertNull(client.webFinger(TEST_ACCT));
    }

    /**  RFC 7033 4.2 */
    @Test
    public void shouldFailOnInvalidCertResponse() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        client.setHttpClient(testHelper.createTrustNoOneHttpClient().build());
        JsonResourceDescriptor jrd = client.webFinger("paulej@packetizer.com");
        assertNull(jrd);
    }

    /**  RFC 7033 4.2 */
    @Test
    public void shouldFailWhenSecureConnectionIsNotEstablished() {
        // here we use the host that breaks up SSL connection
        JsonResourceDescriptor jrd = client.webFinger("brett@onebigfluke.com");
        assertNull(jrd);
    }

    /**  RFC 7033 4.2 */
    @Test
    public void requestShouldSpecifyJRDAcceptHeader() throws IOException {
        setUpToRespondWith("valid_jrd.json");

        client.webFinger("bob@example.com");

        verify(mockHttpClient).execute(argThat(new BaseMatcher<HttpUriRequest>() {
            @Override
            public boolean matches(Object argument) {
                Header[] acceptHeaders = ((HttpUriRequest)argument).getHeaders("Accept");
                if (acceptHeaders == null || acceptHeaders.length == 0) return false;

                for (Header header : acceptHeaders) {
                    if ("application/jrd+json".equals(header.getValue())) return true;
                }

                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Has header Accept: application/jrd+json");
            }
        }));

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
