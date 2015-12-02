package core.type;

import com.google.gson.Gson;

public class NetworkBody {

	public String message;
	public Object object;
	
	public NetworkBody (String message, Object object) {
		this.message = message;
		this.object = object;
	}
	
	public<T> T getObject(Class<T> type) {
		Gson gson = new com.google.gson.Gson();
		this.object = gson.fromJson((String)this.object, type);
		return (T)this.object;
	}
}

	