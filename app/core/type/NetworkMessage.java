package core.type;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import play.Logger;

public class NetworkMessage {

	public static NetworkMessage parseJSON(String msg) {
		
		Gson gson = new com.google.gson.Gson();//GsonBuilder().registerTypeAdapter
			//(Object.class, new NetworkObjectCreator()).create();
			NetworkMessage nMsg = gson.fromJson(msg, NetworkMessage.class);
			return nMsg;
		}
		public NetworkHeader header;
		public NetworkBody body;

	
	
	public NetworkMessage(NetworkHeader header, NetworkBody body) {
		this.header = header;
		this.body = body;
	}

	public String getJSON() {
		Gson gson = new com.google.gson.Gson();
		return gson.toJson(this);
	}

	/*
	public<T> T getBody(Class<T> type) {
		
		gson.fromJson(this.unparseBody, NetworkBody<type>.class);
		return null;
	}*/

	/*
	public<T> T getObject(Class<T> type) {
		return this.body.getObject (type);
	}*/
}
