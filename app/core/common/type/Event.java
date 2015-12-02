package core.common.type;

import com.google.gson.Gson;

public class Event<T> {

	public String message;
	public T object;
	
	public Event(String message, T object) {
		
		this.message = message;
		this.object = object;
	}
}
