package org.btc4all.webfinger;

import org.btc4all.webfinger.pojo.JsonResourceDescriptor;
import org.btc4all.webfinger.pojo.Link;

public class Tester {

	public static void main(String[] args) throws WebfingerClientException {
		WebfingerClient wc = new WebfingerClient(true);
		JsonResourceDescriptor jrd = wc.webFinger("jangkim321@gmail.com");
		String bitcoinAddr = null;
		for (Link l : jrd.getLinks()){
			if (l.getRel().contains("bitcoin")){
				bitcoinAddr = l.getHref().toString();
			}
		}
		if (bitcoinAddr!=null){
			//parse link
			String[] str = 	bitcoinAddr.split(":");
			bitcoinAddr = str[(str.length>2)?1:str.length-1];
		}
		System.out.println(bitcoinAddr);
	}

}
