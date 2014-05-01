package org.btc4all.webfinger;

import java.net.URI;

/**
 * @author Kosta Korenkov <7r0ggy@gmail.com>
 */
public class ResourceNotFoundException extends WebFingerClientException {

    private URI resource;

    public ResourceNotFoundException(URI resource) {
        this.resource = resource;
    }

    public URI getResource() {
        return resource;
    }

    @Override
    public String toString() {
        return "WebFinger resource not found: " + resource.toString();
    }
}
