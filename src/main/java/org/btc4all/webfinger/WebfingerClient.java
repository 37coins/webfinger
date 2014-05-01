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

    protected String discoverHostname(URI resourceUri) throws WebFingerClientException {
        String urlAuthority = resourceUri.getRawAuthority();
        if (urlAuthority != null) {
            return Util.toLowerCase(urlAuthority);
        }

        String[] parts = resourceUri.getRawSchemeSpecificPart().split("@");
        if (parts.length == 1) {
            throw new WebFingerClientException(WebFingerClientException.Reason.INVALID_URI);
        }
        return Util.toLowerCase(parts[parts.length - 1]);
    }

    protected URI getWebFingerURI(URI baseURI, URI resource, String[] relLinks) throws WebFingerClientException {
        try {
            URIBuilder uri = new URIBuilder(baseURI);
            uri.addParameter("resource", resource.toString());
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

    protected void validateProof(URI resourceURI, JsonResourceDescriptor jrd) throws WebFingerClientException {
        try {
            Link delegationLink = jrd.getLinkByRel("http://webfist.org/spec/rel");
            if (delegationLink == null || delegationLink.getProperties().get(new URI("http://webfist.org/spec/proof")) == null) {
                throw new WebFingerClientException(WebFingerClientException.Reason.WEBFIST_NO_PROOF);
            }

            String proofLink = delegationLink.getProperties().get(new URI("http://webfist.org/spec/proof"));

            proofValidator.validate(resourceURI.toString(), proofLink);
        } catch (ProofValidationException e) {
            throw new WebFingerClientException(WebFingerClientException.Reason.WEBFIST_PROOF_VALIDATION_FAILED, e);
        } catch (URISyntaxException e) {
            throw new WebFingerClientException(WebFingerClientException.Reason.WEBFIST_NO_PROOF, e);
        }
    }

    protected URI getResourceURI(String resource) throws WebFingerClientException {
        // prepend default scheme if needed
        if (!resource.matches("^\\w+:.+")) {
            resource = "acct:" + resource;
        }

        try {
            return new URI(resource);
        } catch (URISyntaxException e) {
            throw new WebFingerClientException(WebFingerClientException.Reason.INVALID_URI, e);
        }
    }

    public JsonResourceDescriptor webFinger(String resource, String... rel) throws WebFingerClientException {

        URI resourceURI = getResourceURI(resource);

        HttpGet fingerHttpGet = new HttpGet("https://" + discoverHostname(resourceURI) + "/.well-known/webfinger");
        URI uri = getWebFingerURI(fingerHttpGet.getURI(), resourceURI, rel);
        HttpRequestBase request = new HttpGet(uri);
        request.setHeader("Accept","application/jrd+json");
        JsonResourceDescriptor jrd = getJRD(request);

        if (jrd == null && webfistFallback && resourceURI.getScheme().equals("acct")) {
            HttpGet webFistGet = new HttpGet("https://webfist.org/.well-known/webfinger");
            uri = getWebFingerURI(webFistGet.getURI(), resourceURI, rel);
            HttpRequestBase bitRequest = new HttpGet(uri);
            bitRequest.setHeader("Accept","application/jrd+json");
            jrd = getJRD(bitRequest);

            if (jrd != null && jrd.getLinks() != null){
                validateProof(resourceURI, jrd);
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
            throw new ResourceNotFoundException(resourceURI);
        }

        filterLinks(jrd, rel);

        return jrd;
    }

}
