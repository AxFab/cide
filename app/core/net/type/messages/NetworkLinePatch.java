package core.net.type.messages;

import java.util.ArrayList;
import java.util.List;

import name.fraser.neil.plaintext.diff_match_patch.Patch;

import com.google.gson.annotations.Expose;

public class NetworkLinePatch {

	@Expose
	public String filepath;
	
	@Expose
	public String patches;
	
	@Expose
	public int row;
	
	@Expose
	public String username;
}
