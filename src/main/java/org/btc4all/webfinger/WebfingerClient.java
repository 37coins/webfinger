package org.btc4all.webfinger;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.btc4all.webfinger.pojo.JsonResourceDescriptor;
import org.btc4all.webfinger.pojo.Link;
import org.btc4all.webfinger.util.Util;
import org.btc4all.webfinger.webfist.DKIMProofValidator;
import org.btc4all.webfinger.webfist.ProofValidationException;
import org.btc4all.webfinger.webfist.ProofValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class WebFingerClient {

    private static final Logger log = LoggerFactory.getLogger(WebFingerClient.class);

    private boolean webfistFallback;

    private HttpClient httpClient;

    private ProofValidator proofValidator;

    public WebFingerClient(boolean webfistFallback){
        this(webfistFallback, HttpClientFactory.getClientBuilder().build(), new DKIMProofValidator());
	}

    protected WebFingerClient(boolean webfistFallback, HttpClient httpClient, ProofValidator proofValidator) {
        this.webfistFallback = webfistFallback;
        this.httpClient = httpClient;
        this.proofValidator = proofValidator;
    }

    protected JsonResourceDescriptor parseJRD(HttpResponse response) throws WebFingerClientException {
        try {
            return new ObjectMapper().readValue(response.getEntity().getContent(), JsonResourceDescriptor.class);
        } catch (IOException e){
            throw new WebFingerClientException(WebFingerClientException.Reason.ERROR_PARSING_JRD, e);
        }
    }

	protected JsonResourceDescriptor getJRD(HttpRequestBase request) throws WebFingerClientException {
        HttpResponse response;
        try {
            response = httpClient.execute(request);
        } catch (IOException e) {
            throw new WebFingerClientException(WebFingerClientException.Reason.ERROR_GETTING_RESOURCE, e);
        }

        if (Util.isSucceed(response)){
            return parseJRD(response);
        }
        return null;
	}

    protected String discoverHostname(String resource) throws WebFingerClientException {
        try {
            URI uri = new URI(resource);
            String urlAuthority = uri.getRawAuthority();
            if (urlAuthority != null) {
                return Util.toLowerCase(urlAuthority);
            }

            String[] parts = uri.getRawSchemeSpecificPart().split("@");
            if (parts.length == 1) {
                throw new WebFingerClientException(WebFingerClientException.Reason.INVALID_URI);
            }
            return Util.toLowerCase(parts[parts.length - 1]);
        } catch (URISyntaxException e) {
            throw new WebFingerClientException(WebFingerClientException.Reason.INVALID_URI, e);
        }
    }

    protected URI getWebFingerURI(URI baseURI, String resource, String[] relLinks) throws WebFingerClientException {
        try {
            URIBuilder uri = new URIBuilder(baseURI);
            uri.addParameter("resource", resource);
            for (String rel : relLinks) {
                uri.addParameter("rel", rel);
            }
            return uri.build();
        } catch (URISyntaxException e) {
            throw new WebFingerClientException(WebFingerClientException.Reason.INVALID_URI, e);
        }
    }

    protected void filterLinks(JsonResourceDescriptor jrd, String[] rel) {
        if (rel.length > 0 && jrd.getLinks() != null) {
            List<String> targetLinks = Arrays.asList(rel);
            for (Iterator<Link> it = jrd.getLinks().iterator(); it.hasNext(); ) {
                Link link = it.next();
                if (!targetLinks.contains(link.getRel())) {
                    it.remove();
                }
            }
        }
    }

    protected void validateProof(String resource, JsonResourceDescriptor jrd) throws WebFingerClientException {
        try {
            Link delegationLink = jrd.getLinkByRel("http://webfist.org/spec/rel");
            if (delegationLink == null || delegationLink.getProperties().get(new URI("http://webfist.org/spec/proof")) == null) {
                throw new WebFingerClientException(WebFingerClientException.Reason.WEBFIST_NO_PROOF);
            }

            String proofLink = delegationLink.getProperties().get(new URI("http://webfist.org/spec/proof"));

            proofValidator.validate(resource, proofLink);
        } catch (ProofValidationException e) {
            throw new WebFingerClientException(WebFingerClientException.Reason.WEBFIST_PROOF_VALIDATION_FAILED, e);
        } catch (URISyntaxException e) {
            throw new WebFingerClientException(WebFingerClientException.Reason.WEBFIST_NO_PROOF, e);
        }
    }


    public JsonResourceDescriptor webFinger(String resource, String... rel) throws WebFingerClientException {

        // prepend default scheme if needed
        if (!resource.matches("^\\w+:.+")) {
            resource = "acct:" + resource;
        }

        HttpGet fingerHttpGet = new HttpGet("https://" + discoverHostname(resource) + "/.well-known/webfinger");
        URI uri = getWebFingerURI(fingerHttpGet.getURI(), resource, rel);
        HttpRequestBase request = new HttpGet(uri);
        request.setHeader("Accept","application/jrd+json");
        JsonResourceDescriptor jrd = getJRD(request);

        if (jrd == null && webfistFallback) {
            HttpGet webFistGet = new HttpGet("https://webfist.org/.well-known/webfinger");
            uri = getWebFingerURI(webFistGet.getURI(), resource, rel);
            HttpRequestBase bitRequest = new HttpGet(uri);
            bitRequest.setHeader("Accept","application/jrd+json");
            jrd = getJRD(bitRequest);

            if (jrd != null && jrd.getLinks() != null){
                validateProof(resource, jrd);
                for (Link l : jrd.getLinks()){
                    if (l.getRel().contains("webfist.org/spec/rel")){
                        HttpRequestBase contentRequest = new HttpGet(l.getHref());
                        jrd = getJRD(contentRequest);
                        break;
                    }
                }
            }
        }

        if (jrd == null) {
            throw new ResourceNotFoundException(resource);
        }

        filterLinks(jrd, rel);

        return jrd;
    }

}
