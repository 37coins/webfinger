package org.btc4all.webfinger.matchers;

import org.apache.http.client.methods.HttpUriRequest;
import org.hamcrest.Matcher;
import org.hamcrest.core.IsNot;

/**
 * @author Kosta Korenkov <7r0ggy@gmail.com>
 */
public class Matchers {

    public static Matcher<HttpUriRequest> hasUrl(String url) {
        return new HasUrl(url);
    }

    public static <T> Matcher<T> isNot(Matcher<T> matcher) {
        return new IsNot<>(matcher);
    }

    public static Matcher<HttpUriRequest> hasHttpsScheme() {
        return new HasHttpsScheme();
    }

    public static Matcher<HttpUriRequest> hasHostnameMatching(String regex) {
        return new HasHostnameMatching(regex);
    }

    public static Matcher<HttpUriRequest> hasParameterMatching(String name, String regex) {
        return new HasParameterMatching(name, regex);
    }

}
