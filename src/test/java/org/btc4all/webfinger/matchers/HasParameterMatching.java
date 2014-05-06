package org.btc4all.webfinger.matchers;

import org.apache.http.client.methods.HttpUriRequest;
import org.hamcrest.Description;
import org.mockito.ArgumentMatcher;

import java.util.regex.Pattern;

/**
 * @author Kosta Korenkov <7r0ggy@gmail.com>
 */
public class HasParameterMatching extends ArgumentMatcher<HttpUriRequest> {
    private String regex;
    private String name;

    public HasParameterMatching(String name, String regex) {
        this.name = name;
        this.regex = regex;
    }

    @Override
    public boolean matches(Object o) {
        boolean result = false;

        String uri = ((HttpUriRequest)o).getURI().toString();
        Pattern pattern = Pattern.compile(regex);
        String[] parts = uri.split("[\\?&]");
        for (int i = 0; i < parts.length && !result; i++ ) {
            String[] paramParts = parts[i].split("=");
            if (paramParts.length != 2) continue;
            if (paramParts[0].equals(name)) {
                result = pattern.matcher(paramParts[1]).matches();
            }
        }

        return result;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("Has parameter \"" + name + "\" matching " + regex);
    }
}
