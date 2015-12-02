package core.net.type.messages;

import com.google.gson.annotations.Expose;

public class NetworkLineLock {

	public NetworkLineLock(String filepath, int row, String username) {
		this.filepath = filepath;
		this.row = row;
		this.username = username;
	}

	@Expose
	public String filepath;
	
	@Expose
	public int row;
	
	@Expose
	public String username;
}
