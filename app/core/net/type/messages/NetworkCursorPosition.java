package core.net.type.messages;

import com.google.gson.annotations.Expose;

import core.module.type.Position;

public class NetworkCursorPosition {

	@Expose
	public int row;
	
	@Expose
	public int column;
	
	@Expose
	public String filepath;
	
	@Expose
	public String username;
	
	public NetworkCursorPosition(Position position) {
		
		this.row = position.row;
		this.column = position.column;
	}
}
