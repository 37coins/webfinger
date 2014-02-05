package org.btc4all.webfinger;

import java.io.IOException;
import java.net.URISyntaxException;

import org.btc4all.webfinger.pojo.JsonResourceDescriptor;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Tester {

	/**
	 * @param args
	 * @throws URISyntaxException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws URISyntaxException, IOException {
		WebfingerClient wc = new WebfingerClient(true);
		JsonResourceDescriptor jrd = wc.webFinger("makingabetter@gmail.com");
		System.out.println(new ObjectMapper().writeValueAsString(jrd));
	}

}
