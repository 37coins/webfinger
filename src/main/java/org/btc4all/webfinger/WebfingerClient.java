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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WebfingerClient {
	private boolean webfistFallback;
	private final HttpClient client;
	
	public WebfingerClient(boolean webfistFallback){
		this.webfistFallback = webfistFallback;
		client = HttpClientBuilder.create().build();
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
	
	public JsonResourceDescriptor webFinger(String resource) throws URISyntaxException, IOException{
		JsonResourceDescriptor jrd = null;

		String[] parts = new URI(resource).getRawSchemeSpecificPart().split("@");

		HttpGet fingerHttpGet = new HttpGet("https://"+parts[parts.length-1]+"/.well-known/webfinger");
		URI uri = new URIBuilder(fingerHttpGet.getURI()).addParameter("resource", "acct:"+resource).build();
		HttpRequestBase request = new HttpGet(uri);
		request.setHeader("Accept-Encoding","application/jrd+json");
		jrd = getJRD(request);
			
		if (jrd ==null && webfistFallback){
			HttpGet fistHttpGet = new HttpGet("https://bitfinger.org/.well-known/webfinger");
			URI uri2 = new URIBuilder(fistHttpGet.getURI()).addParameter("resource", "acct:"+resource).build();
			HttpRequestBase fistRequest = new HttpGet(uri2);
			request.setHeader("Accept-Encoding","application/jrd+json");
			jrd = getJRD(fistRequest);
		}
		return jrd;
	}

}
