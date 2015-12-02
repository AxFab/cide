package core.module.type;

public class EditorUserBuffer {

	public class Position {

		int row, column;

		public Position() {
			row = column = 0;
		}

		public void set(int row, int column) {
			this.row = row;
			this.column = column;
		}
	};
	String text;
	Position cursorStart, cursorCurrent;

	boolean isEmpty() {
		return (cursorStart.row == cursorCurrent.row
			&& cursorStart.column == cursorCurrent.column);
	}

	boolean isTextMode() {
		return (!isEmpty() && !text.equals(""));
	}

	boolean isSupprMode() {
		return (!isEmpty() && text.equals(""));
	}
}
