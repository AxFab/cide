package core.net.type.core;

import java.util.LinkedList;
import java.util.List;

import models.User;

import play.Logger;
import play.libs.F.EventStream;
import play.libs.F.Promise;

public class Connection {

	public String username;
	List<NetworkMessage> pendingMessage;
	EventStream<NetworkMessage> eventStream;

	public Connection(String username) {
		
		this.username = username;
		this.pendingMessage = new LinkedList<NetworkMessage>();
		this.eventStream = new EventStream<NetworkMessage>();
	}

	public void sendMessage(NetworkMessage message) {
		eventStream.publish(message);
	}

	public Promise<NetworkMessage> nextOutbountEvent() {
		return eventStream.nextEvent();
	}
}
