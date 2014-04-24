package org.btc4all.webfinger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.btc4all.webfinger.pojo.JsonResourceDescriptor;
import org.btc4all.webfinger.pojo.Link;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebfingerClient {
	private boolean webfistFallback;
	private HttpClient client;
    private static final Logger log = LoggerFactory.getLogger(WebfingerClient.class);

    public WebfingerClient(boolean webfistFallback){
		this.webfistFallback = webfistFallback;
		client = HttpClientBuilder.create().build();
	}

    protected void setHttpClient(HttpClient client) {
        this.client = client;
    }
	
	protected JsonResourceDescriptor getJRD(HttpRequestBase request) throws IOException{
		try{
			HttpResponse response = client.execute(request);
			if (response.getStatusLine().getStatusCode()>=200&&response.getStatusLine().getStatusCode()<300){
				return new ObjectMapper().readValue(response.getEntity().getContent(), JsonResourceDescriptor.class);
			}
		}catch(JsonParseException | JsonMappingException | ClientProtocolException ex){
			ex.printStackTrace();
		}
		return null;
	}
	
	public JsonResourceDescriptor webFinger(String resource) {
        JsonResourceDescriptor jrd = null;
        try {
            String[] parts = new URI(resource).getRawSchemeSpecificPart().split("@");

            HttpGet fingerHttpGet = new HttpGet("https://"+parts[parts.length-1]+"/.well-known/webfinger");
            URI uri = new URIBuilder(fingerHttpGet.getURI()).addParameter("resource", "acct:"+resource).build();
            HttpRequestBase request = new HttpGet(uri);
            request.setHeader("Accept-Encoding","application/jrd+json");
            jrd = getJRD(request);

            if (jrd ==null && webfistFallback){
                HttpGet bitHttpGet = new HttpGet("https://bitfinger.org/.well-known/webfinger");
                URI uri2 = new URIBuilder(bitHttpGet.getURI()).addParameter("resource", "acct:"+resource).build();
                HttpRequestBase bitRequest = new HttpGet(uri2);
                bitRequest.setHeader("Accept-Encoding","application/jrd+json");
                jrd = getJRD(bitRequest);
                //TODO: verify proof and DKIM
                //resolve content
                if (null!=jrd && null!=jrd.getLinks()){
                    for (Link l : jrd.getLinks()){
                        if (l.getRel().contains("webfist.org/spec/rel")){
                            HttpGet contentHttpGet = new HttpGet(l.getHref());
                            URI contentUri = new URIBuilder(contentHttpGet.getURI()).build();
                            HttpRequestBase contentRequest = new HttpGet(contentUri);
                            contentRequest.setHeader("Accept-Encoding","application/javascript");
                            jrd = getJRD(contentRequest);
                            break;
                        }
                    }
                }

            }
        } catch (IOException | URISyntaxException e) {
            log.error("Wenfinger query failed to URI:" + resource, e);
        }
        return jrd;
    }

}
