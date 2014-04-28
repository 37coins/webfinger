package org.btc4all.webfinger;

/**
 * @author Kosta Korenkov <7r0ggy@gmail.com>
 */
public class WebFingerClientException extends Exception {

    private Reason reason;

    public WebFingerClientException() {
    }

    public WebFingerClientException(Reason reason) {
        this.reason = reason;
    }

    public WebFingerClientException(Reason reason, Throwable cause) {
        super(cause);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }

    public enum Reason {
        INVALID_URI,
        ERROR_GETTING_RESOURCE,
        ERROR_PARSING_JRD
    }
}
