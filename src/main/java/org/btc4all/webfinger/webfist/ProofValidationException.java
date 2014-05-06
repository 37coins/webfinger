package org.btc4all.webfinger.webfist;

/**
 * @author Kosta Korenkov <7r0ggy@gmail.com>
 */
public class ProofValidationException extends Exception {

    private Reason reason;

    public ProofValidationException(Reason reason) {
        this.reason = reason;
    }

    public ProofValidationException(Reason reason, Throwable cause) {
        super(cause);
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "ProofValidationException{" +
                "reason=" + reason +
                '}';
    }

    public enum Reason {
        ERROR_CHECKING_PROOF, PROOF_CHECK_FAILED, NO_PROOF
    }
}
