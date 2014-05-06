package org.btc4all.webfinger.matchers;

import org.apache.http.client.methods.HttpUriRequest;
import org.hamcrest.Description;
import org.mockito.ArgumentMatcher;

/**
 * @author Kosta Korenkov <7r0ggy@gmail.com>
 */
public class HasUrl extends ArgumentMatcher<HttpUriRequest> {
    private String url;

    public HasUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean matches(Object o) {
        return o != null && ((HttpUriRequest) o).getURI().toString().startsWith(url);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("Has URL ").appendValue(url);
    }
}
