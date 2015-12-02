package core;

import core.type.NetworkMessage;
import java.util.*;
import play.Logger;

public class ConnectionManager {

	static List<Connection> connectedUser = new ArrayList<Connection>();

	public static Connection openConnection(String username) {
		Connection user = new Connection(username);
		if (!user.isValid()) {
			return null;
		}
		connectedUser.add(user);
		Logger.info ("ConnectionManager, new user [" + connectedUser.size() + "]");
		return user;
	}

	public static Connection getConnection(String username) {
		for (Connection user : connectedUser) {
			if (user.user.equals(username)) {
				return user;
			}
		}
		return null;
	}
	
	public static void closeConnection(String username) {
		for (Connection user : connectedUser) {
			if (user.user.equals(username)) {
				connectedUser.remove(user);
				Logger.info ("ConnectionManager, delete user [" + connectedUser.size() + "]");
				return;
			}
		}
	}

	public static void sendMessage(NetworkMessage message) {
		Logger.debug ("We ask to send a message to user:" + message.header.user);
		Connection connect = getConnection(message.header.user);
		if (connect != null) {
			Logger.debug ("We find the connection objet related to this user");
			connect.sendMessage(message);
		} else {
			Logger.info("Connection manager can't find user: " + message.header.user);
		}
	}

	public static void broadcast(NetworkMessage message) {
		for (Connection user : connectedUser) {
			user.sendMessage(message);
		}
	}
}
