package org.btc4all.webfinger.matchers;

import org.apache.http.client.methods.HttpUriRequest;
import org.hamcrest.Description;
import org.mockito.ArgumentMatcher;

import java.util.regex.Pattern;

/**
 * @author Kosta Korenkov <7r0ggy@gmail.com>
 */
public class HasHttpsScheme extends ArgumentMatcher<HttpUriRequest> {

    @Override
    public boolean matches(Object o) {
        return Pattern.matches("^https.*", ((HttpUriRequest) o).getURI().toString());
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("Has HTTPS scheme in URL");
    }
}
