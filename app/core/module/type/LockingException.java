package core.module.type;

import models.User;

import com.google.gson.annotations.Expose;

import core.common.type.CideException;

public class LockingException extends CideException{
	
	public enum LockingExceptionType {
		ALREADY_LOCKED, OBTAIN_LOCK
	};
	
	/** Type of the exception */
	public LockingExceptionType type;
	
	/** Line where locking error occured */
	@Expose
	public int row;
	
	/** User already owning the lock if any */
	@Expose
	public String username;
	
	public LockingException(Throwable t) {
		super(t);
	}
	
	public LockingException(LockingExceptionType type,	
			int row, String username) {
		
		super(type.name());
		this.type = type;
		this.row = row;
		this.username = username;
	}

}
