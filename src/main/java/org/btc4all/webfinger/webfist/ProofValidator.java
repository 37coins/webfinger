package org.btc4all.webfinger.webfist;

import java.net.URI;

/**
 * @author Kosta Korenkov <7r0ggy@gmail.com>
 */
public interface ProofValidator {

    void validate(URI resource, String proofLink) throws ProofValidationException;
}
