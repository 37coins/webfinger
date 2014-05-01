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

    @Override
    public String toString() {
        return "WebFingerClientException{" +
                "reason=" + reason +
                '}';
    }

    public enum Reason {
        INVALID_URI,
        ERROR_GETTING_RESOURCE,
        WEBFIST_PROOF_VALIDATION_FAILED, WEBFIST_NO_PROOF, ERROR_PARSING_JRD
    }
}
