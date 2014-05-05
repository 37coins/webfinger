package org.btc4all.webfinger.webfist;

import org.apache.http.client.HttpClient;
import org.apache.james.jdkim.DKIMVerifier;
import org.apache.james.jdkim.exceptions.FailException;
import org.btc4all.webfinger.helpers.MockHelper;
import org.btc4all.webfinger.helpers.Response;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InOrder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import static org.btc4all.webfinger.matchers.Matchers.hasUrl;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.*;

/**
 * @author Kosta Korenkov <7r0ggy@gmail.com>
 */
@RunWith(JUnit4.class)
public class DKIMProofValidatorTest {

    private ProofValidator validator;

    private HttpClient mockHttpClient;

    private String proofLink = "http://webfist.org/webfist/proof/08e01fb3123de74555528daaeb2d33b513f50f88-c255b91b02617c067df89a3809f0e17197b52413?decrypt=pithy.example%40gmail.com";

    @Before
    public void setUp() throws Exception {
        mockHttpClient = mock(HttpClient.class);
        validator = new DKIMProofValidator(mockHttpClient);

        when(mockHttpClient.execute(argThat(hasUrl("http://webfist.org/webfist/proof/"))))
                .thenReturn(Response.OKResponseWithDataFromFile("proof_email_for-pithy.example@gmail.com.eml"));
    }

    @Test (expected = ProofValidationException.class)
    public void shouldFailIfProofIsOfDifferentSender() throws ProofValidationException, URISyntaxException {
        validator.validate(new URI("jangkim321@gmail.com"), proofLink);
    }

    @Test (expected = ProofValidationException.class)
    public void shouldFailIfProofIsSignedByDifferentDomain() throws ProofValidationException, IOException, FailException, URISyntaxException {
        when(mockHttpClient.execute(argThat(hasUrl("http://webfist.org/webfist/proof/"))))
                .thenReturn(Response.OKResponseWithDataFromFile("proof_email_for-pithy.example@yahoo.com-signed-by-gmail.eml"));

        DKIMVerifier mockVerifier = mock(DKIMVerifier.class);
        when(mockVerifier.verify(any(InputStream.class))).thenReturn(Arrays.asList(MockHelper.getGmailSignature()));
        validator = new DKIMProofValidator(mockHttpClient, mockVerifier);

        validator.validate(new URI("pithy.example@yahoo.com"), proofLink);
    }

    @Test (expected = ProofValidationException.class)
    public void shouldFailIfProofEmailIsNotSigned() throws ProofValidationException, IOException, URISyntaxException {
        when(mockHttpClient.execute(argThat(hasUrl("http://webfist.org/webfist/proof/"))))
                .thenReturn(Response.OKResponseWithDataFromFile("proof_email_without_DKIM.eml"));

        validator.validate(new URI("pithy.example@gmail.com"), proofLink);
    }

    @Test
    public void shouldFailIfDKIMValidationFails() throws IOException {
        List<String> proofEmailFiles = Arrays.asList("proof_email_w_spoofed_body.eml", "proof_email_w_spoofed_sender.eml");

        for (String proofEmail : proofEmailFiles) {
            when(mockHttpClient.execute(argThat(hasUrl("http://webfist.org/webfist/proof/"))))
                    .thenReturn(Response.OKResponseWithDataFromFile(proofEmail));

            try {
                validator.validate(new URI("pithy.example@gmail.com"), proofLink);
                fail("Expected ProofValidationException");
            } catch (Exception e) {
                assertEquals(ProofValidationException.class, e.getClass());
            }
        }
    }

    @Test
    public void shouldFetchProofEmail() throws IOException, ProofValidationException, URISyntaxException {

        validator.validate(new URI("pithy.example@gmail.com"), proofLink);

        InOrder inOrder = inOrder(mockHttpClient);
        inOrder.verify(mockHttpClient, times(1)).execute(argThat(hasUrl(proofLink)));
    }

    @Test
    public void shouldValidateProperProof() throws URISyntaxException, ProofValidationException {
        validator.validate(new URI("pithy.example@gmail.com"), proofLink);
    }

}
