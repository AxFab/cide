package core.common.type;

public class CideException extends Exception {

	public CideException(String msg, Throwable t) {
		
		super(msg, t);
	}
	
	public CideException(String msg) {
		
		super(msg);
	}
	
	public CideException(Throwable t) {
		
		super(t);
	}
}
