package org.btc4all.webfinger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.btc4all.webfinger.pojo.JsonResourceDescriptor;
import org.btc4all.webfinger.pojo.Link;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebFingerClient {

    private static final Logger log = LoggerFactory.getLogger(WebFingerClient.class);

    private boolean webfistFallback;

    private HttpClient httpClient;


    public WebFingerClient(boolean webfistFallback){
		this.webfistFallback = webfistFallback;
		httpClient = HttpClientFactory.getClientBuilder().build();
	}

    protected void setHttpClient(HttpClient client) {
        this.httpClient = client;
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

        if (response.getStatusLine().getStatusCode() >= 200 && response.getStatusLine().getStatusCode() < 300){
            return parseJRD(response);
        }
        return null;
	}

    protected String discoverHostname(String resource) throws URISyntaxException, WebFingerClientException {
        URI uri = new URI(resource);
        String urlAuthority = uri.getRawAuthority();
        if (urlAuthority != null) {
            return toLowerCase(urlAuthority);
        }

        String[] parts = uri.getRawSchemeSpecificPart().split("@");
        if (parts.length == 1) {
            throw new WebFingerClientException(WebFingerClientException.Reason.INVALID_URI);
        }
        return toLowerCase(parts[parts.length - 1]);
    }

    private String toLowerCase(String str) {
        return !str.contains("%") ? str.toLowerCase() : str;
    }

    public JsonResourceDescriptor webFinger(String resource) throws WebFingerClientException {
        JsonResourceDescriptor jrd = null;
        try {
            // prepend default scheme if needed
            if (!resource.matches("^\\w+:.+")) {
                resource = "acct:" + resource;
            }

            HttpGet fingerHttpGet = new HttpGet("https://" + discoverHostname(resource) + "/.well-known/webfinger");
            URI uri = new URIBuilder(fingerHttpGet.getURI()).addParameter("resource", resource).build();
            HttpRequestBase request = new HttpGet(uri);
            request.setHeader("Accept","application/jrd+json");
            jrd = getJRD(request);

            if (jrd == null) {
                if (!webfistFallback) {
                    throw new ResourceNotFoundException(resource);
                }

                HttpGet bitHttpGet = new HttpGet("https://webfist.org/.well-known/webfinger");
                URI uri2 = new URIBuilder(bitHttpGet.getURI()).addParameter("resource", resource).build();
                HttpRequestBase bitRequest = new HttpGet(uri2);
                bitRequest.setHeader("Accept","application/jrd+json");
                jrd = getJRD(bitRequest);
                //TODO: verify proof and DKIM
                //resolve content
                if (null!=jrd && null!=jrd.getLinks()){
                    for (Link l : jrd.getLinks()){
                        if (l.getRel().contains("webfist.org/spec/rel")){
                            HttpGet contentHttpGet = new HttpGet(l.getHref());
                            URI contentUri = new URIBuilder(contentHttpGet.getURI()).build();
                            HttpRequestBase contentRequest = new HttpGet(contentUri);
                            jrd = getJRD(contentRequest);
                            break;
                        }
                    }
                }
            }

        } catch (URISyntaxException e) {
            log.error("Wenfinger query failed to URI:" + resource, e);
        }
        return jrd;
    }

}
