package core.net.type.messages;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import name.fraser.neil.plaintext.diff_match_patch.Operation;
import models.User;
import name.fraser.neil.plaintext.diff_match_patch.Patch;

import com.google.gson.annotations.Expose;

import core.module.type.LineDiff;

public class NetworkPatch {
	@Expose
	/**
	 * One patch per modified line
	 */
	public Map<Integer, String> patches;
	@Expose
	public List<NetworkLine> lineChanges;
	
	public NetworkPatch(Map<Integer, String> patches, List<LineDiff> lineChanges) {
		this.patches = new HashMap<Integer, String> (patches);
		this.lineChanges = new LinkedList<NetworkLine> ();
		
		for (LineDiff ld: lineChanges) {
			NetworkLine nl = new NetworkLine();
			
			nl.isInsertOperation = (ld.o == Operation.INSERT);
			nl.row = ld.row;
			
			this.lineChanges.add(nl);
		}
	}
}
