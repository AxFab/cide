package core.module.type;

import name.fraser.neil.plaintext.diff_match_patch.Operation;

public class LineDiff {
	public final Operation o;
	public final int row;
	
	public LineDiff (Operation o, int row) {
		this.o = o;
		this.row = row;
	}
}
