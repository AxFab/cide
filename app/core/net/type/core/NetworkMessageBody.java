package core.net.type.core;

import play.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;

public class NetworkMessageBody {

	@Expose
	public String message;
	
	@Expose
	public Object object;
	
	public JsonElement jsonElement;
	
	public NetworkMessageBody(String message) {
	    
		this.message = message;
	}

	public NetworkMessageBody(String message, Object object) {
		
		this.message = message;
		this.object = object;
	}

	public <T> T getObject(Class<T> type) throws JsonSyntaxException {
		
		if(jsonElement != null) {
			this.object = NetworkMessage.gson.fromJson(jsonElement, type);
		} else {
			this.object = null;
		}
		
		return (T) this.object;
	}
}
