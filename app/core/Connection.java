package core;

import core.type.NetworkMessage;
import java.util.List;
import java.util.LinkedList;
import play.Logger;
import play.libs.F.EventStream;
import play.libs.F.Promise;

public class Connection {

	String user;
	List<NetworkMessage> pendingMessage;
	EventStream<NetworkMessage> eventStream;

	public Connection(String user) {
		this.user = user;
		this.pendingMessage = new LinkedList<NetworkMessage>();
		this.eventStream = new EventStream<NetworkMessage>();
	}

	public boolean isValid() {
		return true;
	}

	public void sendMessage(NetworkMessage message) {
		Logger.info("[" + user + "] Send message: " + message.getJSON());
		eventStream.publish(message);
	}

	public Promise<NetworkMessage> nextOutbountEvent() {
		return eventStream.nextEvent();
	}
}
