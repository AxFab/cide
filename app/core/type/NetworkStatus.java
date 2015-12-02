package core.type;

import core.ConnectionManager;

public class NetworkStatus extends NetworkMessage {
	public NetworkStatus(String user, String source, Status status) {
		super(new NetworkHeader(user, "broadcast", source), new NetworkBody("Error " + status.toString(), null));
	}

	public static void sendStatus(String user, String id, Status status) {
		NetworkStatus msg = new NetworkStatus(user, id, status);
		ConnectionManager.sendMessage(msg);
	}

	public enum Status {

		UserNotFound, UserConnected, ErronedNetworkMessage
	}
}
