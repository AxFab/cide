package core.module.type;

import core.net.type.messages.NetworkCursorPosition;

public class Position {
	public int row;
	public int column;
	
	public Position(int row, int column) {
		this.row = row;
		this.column = column;
	}
	
	public Position(NetworkCursorPosition pos) {
		this.row = pos.row;
		this.column = pos.column;
	}
}
