package core.net.type.messages;

import com.google.gson.annotations.Expose;

public class NetworkChatMessage {

	@Expose
	public String username;
	
	@Expose
	public String message;
	
	@Expose
	public String filepath;
}
