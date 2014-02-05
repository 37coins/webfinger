package org.btc4all.webfinger.pojo;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
public class JsonResourceDescriptor {
	
	private URI subject;
	private Set<URI> aliases;
	private Map<URI,String> properties;
	private List<Link> links;
	public URI getSubject() {
		return subject;
	}
	public JsonResourceDescriptor setSubject(URI subject) {
		this.subject = subject;
		return this;
	}
	public Set<URI> getAliases() {
		return aliases;
	}
	public JsonResourceDescriptor setAliases(Set<URI> aliases) {
		this.aliases = aliases;
		return this;
	}
	public Map<URI, String> getProperties() {
		return properties;
	}
	public JsonResourceDescriptor setProperties(Map<URI, String> properties) {
		this.properties = properties;
		return this;
	}
	public List<Link> getLinks() {
		return links;
	}
	public JsonResourceDescriptor setLinks(List<Link> links) {
		this.links = links;
		return this;
	}

}
