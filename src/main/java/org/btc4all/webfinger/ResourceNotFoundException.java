package org.btc4all.webfinger;

/**
 * @author Kosta Korenkov <7r0ggy@gmail.com>
 */
public class ResourceNotFoundException extends WebFingerClientException {

    private String resource;

    public ResourceNotFoundException(String resource) {
        this.resource = resource;
    }

    public String getResource() {
        return resource;
    }

    @Override
    public String toString() {
        return "WebFinger resource not found: " + resource;
    }
}
