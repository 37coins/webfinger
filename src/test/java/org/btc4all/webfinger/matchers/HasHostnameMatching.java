package org.btc4all.webfinger.matchers;

import org.apache.http.client.methods.HttpUriRequest;
import org.hamcrest.Description;
import org.mockito.ArgumentMatcher;

import java.util.regex.Pattern;

/**
 * @author Kosta Korenkov <7r0ggy@gmail.com>
 */
public class HasHostnameMatching extends ArgumentMatcher<HttpUriRequest> {
    private String name;

    public HasHostnameMatching(String name) {
        this.name = name;
    }

    @Override
    public boolean matches(Object o) {
        return Pattern.matches("^\\w+?://" + name + "/.*", ((HttpUriRequest) o).getURI().toString());
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("Has hostname matching ").appendValue(name);
    }
}
