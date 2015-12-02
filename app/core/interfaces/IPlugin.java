package core.interfaces;

import core.type.NetworkMessage;
import data.Event;

/**
 * A generic plugin
 */
public interface IPlugin {
	/**
	 * 
	 * @return Unique id for plugin
	 */
	public String id();
	/**
	 * Initialization code 
	 */
	public void start();
	/**
	 * Receive a message from a widget
	 * @param msg
	 */
	public void onNetworkMessage(NetworkMessage msg);
	/**
	 * Receive an event that was previously subscribed to
	 * @param evt
	 * @param data
	 */
	public void onEvent (Event evt, Object data);
}
