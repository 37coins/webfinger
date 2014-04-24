package org.btc4all.webfinger.helpers;

/**
 * @author Kosta Korenkov <7r0ggy@gmail.com>
 */
public class MockData {
    private String uri;

    private String response;

    private String contentType;

    public MockData(String uri, String response) {
        this.uri = uri;
        this.response = response;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
