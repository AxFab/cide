package core.common.type;

import core.common.controller.ModuleManager;
import core.net.type.core.NetworkMessage;
import core.common.type.CideException;

/**
 * A generic plugin
 */
public abstract class Module {
	
	public ModuleManager mm;

	/**
	 * TODO: Remove this?
	 * @return Unique id for plugin
	 */
	public String id() {
		return this.getClass().getCanonicalName();
	}

	/**
	 * Initialization method executed on module registration 
	 * @param mm Project's module manager
	 */
	public abstract void onRegister(ModuleManager mm) throws CideException;
	
	/**
	 * Destruction method on module unregistration
	 */
	public abstract void onUnregister();

	/**
	 * Receive an event that was previously subscribed to
	 * @param evt
	 * @param data
	 */
	public abstract void onEvent(Event evt) throws CideException;

	/**
	 * Receive a message from a widget
	 * @param msg
	 * @throws CideException 
	 * @throws  
	 */
	public abstract void onNetworkMessage(NetworkMessage msg) throws CideException;
}
