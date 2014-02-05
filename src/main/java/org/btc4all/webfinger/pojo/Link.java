package org.btc4all.webfinger.pojo;

import java.net.URI;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
public class Link {
	
	private String rel;
	private URI href;
	private String type;
	private Map<String,String> titles;
	private Map<URI,String> properties;
	public String getRel() {
		return rel;
	}
	public Link setRel(String rel) {
		this.rel = rel;
		return this;
	}
	public URI getHref() {
		return href;
	}
	public Link setHref(URI href) {
		this.href = href;
		return this;
	}
	public String getType() {
		return type;
	}
	public Link setType(String type) {
		this.type = type;
		return this;
	}
	public Map<String, String> getTitles() {
		return titles;
	}
	public Link setTitles(Map<String, String> titles) {
		this.titles = titles;
		return this;
	}
	public Map<URI, String> getProperties() {
		return properties;
	}
	public Link setProperties(Map<URI, String> properties) {
		this.properties = properties;
		return this;
	}
	
	
}
