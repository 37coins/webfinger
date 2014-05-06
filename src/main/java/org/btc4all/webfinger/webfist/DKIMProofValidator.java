package org.btc4all.webfinger.webfist;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.james.jdkim.DKIMVerifier;
import org.apache.james.jdkim.api.SignatureRecord;
import org.apache.james.jdkim.exceptions.FailException;
import org.btc4all.webfinger.HttpClientFactory;
import org.btc4all.webfinger.pojo.JsonResourceDescriptor;
import org.btc4all.webfinger.pojo.Link;
import org.btc4all.webfinger.util.Util;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Properties;

/**
 * @author Kosta Korenkov <7r0ggy@gmail.com>
 */
public class DKIMProofValidator implements ProofValidator {

    private HttpClient httpClient;

    private DKIMVerifier verifier;

    public DKIMProofValidator() {
        this(HttpClientFactory.getClientBuilder().build(), new DKIMVerifier());
    }

    protected DKIMProofValidator(HttpClient httpClient) {
        this(httpClient, new DKIMVerifier());
    }

    protected DKIMProofValidator(HttpClient httpClient, DKIMVerifier verifier) {
        this.httpClient = httpClient;
        this.verifier = verifier;
    }

    protected boolean isSameFromAddress(String proofEmail, URI resource) throws ProofValidationException {
        try {
            Session s = Session.getDefaultInstance(new Properties());
            MimeMessage message = new MimeMessage(s, new ByteArrayInputStream(proofEmail.getBytes()));

            String[] addressParts = message.getFrom()[0].toString().split("<");
            String fromAddress = addressParts[addressParts.length - 1].replace(">","");

            return fromAddress.equalsIgnoreCase(resource.getRawSchemeSpecificPart());
        } catch (MessagingException e) {
            throw new ProofValidationException(ProofValidationException.Reason.ERROR_CHECKING_PROOF, e);
        }

    }

    protected boolean isSenderFromSigningDomain(List<SignatureRecord> signatures, URI resource) {
        String domain = "@" + resource.getRawSchemeSpecificPart().split("@")[1];
        for (SignatureRecord signature : signatures) {
            if (signature.getIdentity().equals(domain)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public void validate(URI resource, String proofLink) throws ProofValidationException {
        try {
            HttpResponse response = httpClient.execute(new HttpGet(proofLink));
            if (!Util.isSucceed(response) || response.getEntity() == null) {
                throw new ProofValidationException(ProofValidationException.Reason.NO_PROOF);
            }
            String proofEmail = IOUtils.toString(response.getEntity().getContent());

            List<SignatureRecord> signatures = verifier.verify(new ByteArrayInputStream(proofEmail.getBytes()));

            if (signatures == null || !isSameFromAddress(proofEmail, resource)
                    || !isSenderFromSigningDomain(signatures, resource)) {
                throw new ProofValidationException(ProofValidationException.Reason.PROOF_CHECK_FAILED);
            }
        } catch (FailException e) {
            throw new ProofValidationException(ProofValidationException.Reason.PROOF_CHECK_FAILED, e);
        } catch (IOException e) {
            throw new ProofValidationException(ProofValidationException.Reason.ERROR_CHECKING_PROOF, e);
        }
    }
}
