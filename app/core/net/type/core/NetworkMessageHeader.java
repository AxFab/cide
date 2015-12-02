package core.net.type.core;

import models.User;

import com.google.gson.annotations.Expose;


public class NetworkMessageHeader {

	@Expose
	public String destination;
	
	@Expose
	public String source;
	
	@Expose
	public String username;

	public NetworkMessageHeader(String user, String destination, String source) {
		
		this.destination = destination;
		this.source = source;
		this.username = user;
	}
}
