package core.net.type.core;

import java.lang.reflect.Modifier;

import models.User;

import play.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.Expose;


public class NetworkMessage {
	@Expose
	public NetworkMessageHeader header;
	
	@Expose
	public NetworkMessageBody body;
	
	static Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
	static JsonParser parser = new JsonParser();
	
	private NetworkMessage() {
	}

	public static NetworkMessage parseJSON(String msg) {

		NetworkMessage nMsg = new NetworkMessage();

		JsonObject rootObject = parser.parse(msg).getAsJsonObject();
		
		JsonObject headerObject = rootObject.get("header").getAsJsonObject();
		JsonObject bodyObject = rootObject.get("body").getAsJsonObject();

		JsonElement userElement = headerObject.get("user");
		String username = null;
		if(userElement != null) {
			username = userElement.getAsString();
		}
		String destination = headerObject.get("destination").getAsString();
		String source = headerObject.get("source").getAsString();
		
		nMsg.header = new NetworkMessageHeader(username, destination, source);
		
		String message = bodyObject.get("message").getAsString();
		nMsg.body = new NetworkMessageBody(message);
		nMsg.body.jsonElement = bodyObject.get("object");
		
		return nMsg;
	}

	public NetworkMessage(NetworkMessageHeader header, NetworkMessageBody body) {
		
		this.header = header;
		this.body = body;
	}

	public NetworkMessage(String user, String destination, String source,
			String command, Object object) {

		this.header = new NetworkMessageHeader(user, destination, source);
		this.body = new NetworkMessageBody(command, object);
	}

	public NetworkMessage(NetworkMessageHeader header, String command,
			Object object) {

		this.header = header;
		this.body = new NetworkMessageBody(command, object);
	}

	public String getJSON() {

		return gson.toJson(this);
		//Logger.debug("[NetworkMessage] JSON to parse: ")
	}

	/**
	 * Create the 'empty' message to answer to a NetworkMessage
	 * 
	 * @param message
	 * @param msg
	 * @return
	 */
	public NetworkMessage createAnswer() {

		NetworkMessageHeader header = new NetworkMessageHeader(
				this.header.username, this.header.source, this.header.destination);
		NetworkMessageBody body = new NetworkMessageBody("", "");

		return new NetworkMessage(header, body);
	}
}
