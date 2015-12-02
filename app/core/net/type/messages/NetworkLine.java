package core.net.type.messages;

import com.google.gson.annotations.Expose;

public class NetworkLine {
	
	@Expose
	public String filepath;
	
	@Expose
	public int row;
	
	@Expose
	public String lineContent;
	
	@Expose
	public boolean isInsertOperation;
}
