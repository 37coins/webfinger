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

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.btc4all.webfinger.matchers.Matchers.*;
import static org.junit.Assert.*;

/**  @author Kosta Korenkov <7r0ggy@gmail.com> */
@RunWith(JUnit4.class)
public class WebFingerBasicTest extends AbstractWebfingerClientTest {


    /**  RFC 7033 4.4 */
    @Test
    public void shouldIgnoreUnknownJRDMembers() throws WebFingerClientException {
        setUpToRespondWith("unwanted_member_jrd.json");
        JsonResourceDescriptor jrd = client.webFinger(TEST_ACCT);
        assertNotNull(jrd);
    }

    /**  RFC 7033 4.4 */
    @Test
    public void shouldAcceptResponseWithMinimalJRD() throws WebFingerClientException {
        setUpToRespondWith("minimal_jrd.json");
        JsonResourceDescriptor jrd = client.webFinger(TEST_ACCT);
        assertNull(jrd.getAliases());
        assertNull(jrd.getProperties());
        assertNull(jrd.getLinks());
    }

    /**  RFC 7033 4.4 */
    @Test
    public void shouldAcceptResponseWithLinksWithoutTypeAndHref() throws WebFingerClientException {
        setUpToRespondWith("valid_jrd.json");
        JsonResourceDescriptor jrd = client.webFinger(TEST_ACCT);
        assertEquals("http://webfinger.example/rel/no-href-link", jrd.getLinks().get(1).getRel());
        assertNull(jrd.getLinks().get(1).getType());
        assertNull(jrd.getLinks().get(1).getHref());
    }

    /**  RFC 7033 4.4 */
    @Test
    public void shouldReturnAllKnownJRDMembers() throws WebFingerClientException {
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
    public void shouldPreferTheLastLanguageTagTitleValueIfSeveralExistForTheSameTag() throws WebFingerClientException {
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
    public void shouldUseOnlyHttps() throws IOException, WebFingerClientException {
        setUpToRespondWith("valid_jrd.json");
        client.webFinger(TEST_ACCT);
        verifyHttpClientExecutedWithArgThat(hasHttpsScheme());
    }

    /**  RFC 7033 4.1 */
    @Test
    public void requestShouldContainResourceParameterInQuery() throws IOException, WebFingerClientException {
        setUpToRespondWith("valid_jrd.json");
        client.webFinger(TEST_ACCT);
        verifyHttpClientExecutedWithArgThat(hasParameterMatching("resource", ".*"));
    }

    /**  RFC 7033 8.1 */
    @Test
    public void requestShouldContainURIScheme() throws IOException, WebFingerClientException {
        setUpToRespondWith("valid_jrd.json");
        client.webFinger("bob@example.com");
        verifyHttpClientExecutedWithArgThat(hasParameterMatching("resource", "^\\w+%3A.*"));
    }

    @Test
    public void shouldWorkWithResourcesWithScheme() throws IOException, WebFingerClientException {
        setUpToRespondWith("valid_jrd.json");
        client.webFinger("acct:bob@example.com");
        verifyHttpClientExecutedWithArgThat(hasParameterMatching("resource", "acct%3Abob%40example\\.com"));
    }

    @Test
    public void shouldWorkForHttpResources() throws IOException, WebFingerClientException {
        setUpToRespondWith("valid_jrd.json");

        client.webFinger("http://example.com/bob");
        verifyHttpClientExecutedWithArgThat(hasParameterMatching("resource", "http%3A%2F%2Fexample\\.com%2Fbob"));
    }


    /**  RFC 7033 4.1 */
    @Test
    public void requestParametersShouldBePercentEncoded() throws IOException, WebFingerClientException {
        setUpToRespondWith("valid_jrd.json");
        client.webFinger("bob@example.com");
        verifyHttpClientExecutedWithArgThat(hasParameterMatching("resource", "acct%3Abob%40example\\.com"));
    }

    /**  RFC 7033 4.2 */
    @Test
    public void shouldFailOnErrorResponse() {
        List<Integer> errorStatusCodes = Arrays.asList(400, 401, 403, 404, 500, 503);

        for (Integer statusCode : errorStatusCodes) {
            setUpToRespondWith(Response.create(statusCode));
            try {
                client.webFinger(TEST_ACCT);
            } catch (Exception e) {
                assertEquals(ResourceNotFoundException.class, e.getClass());
            }
        }
    }

    /**  RFC 7033 4.2 */
    @Test
    public void requestShouldSpecifyJRDAcceptHeader() throws IOException, WebFingerClientException {
        setUpToRespondWith("valid_jrd.json");

        client.webFinger("bob@example.com");

        verifyHttpClientExecutedWithArgThat(new BaseMatcher<HttpUriRequest>() {
            @Override
            public boolean matches(Object argument) {
                Header[] acceptHeaders = ((HttpUriRequest) argument).getHeaders("Accept");
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
        });

    }



}
