package core.net.controller;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import models.User;

import play.Logger;
import play.db.jpa.JPA;
import core.net.type.core.Connection;
import core.net.type.core.NetworkMessage;

public class ConnectionManager {

	static List<Connection> connectedUser = new ArrayList<Connection>();

	/**
	 * Initialize a new connection
	 * 
	 * @param username
	 * @return
	 */
	public static Connection openConnection(String username) {

		Connection connection = new Connection(username);

		connectedUser.add(connection);
		Logger.info("N = '" + username
				+ "'");
		return connection;
	}

	/**
	 * Get connection for a given user
	 * 
	 * @param username
	 * @return
	 */
	public static Connection getConnection(String username) {

		for (Connection conn : connectedUser) {
			if (conn.username.equals(username)) {
				return conn;
			}
		}

		return null;
	}

	/**
	 * Close connection for a given user
	 * 
	 * @param username
	 */
	public static void closeConnection(String username) {
		
		for (Connection conn : connectedUser) {
			if (conn.username.equals(username)) {
				
				connectedUser.remove(conn);
				Logger.info("C ! '"
						+ username + "'");
				return;
			}
		}
	}

	/**
	 * Send a message
	 * 
	 * @param message
	 *            TODO multiple connections per user ?
	 */
	public static void sendMessage(NetworkMessage message) {

		//EntityManager em = JPA.newEntityManager();
		//TypedQuery<User> query = em.createQuery("from User where username='"+message.header.user+"'", User.class);
		//User user = query.getSingleResult();
		String username = message.header.username;
		if (username == null) {
			Logger.error(
					"[ConnectionManager] Unable to send to unexisting user %s",
					message.header.username);
		}

		Connection connect = getConnection(username);
		if (connect == null) {
			Logger.info("[ConnectionManager] No connection is registered for user '"
					+ message.header.username + "'");
		} else {
			//Logger.debug("[ConnectionManager] Connection found for user %s",
			//		username);
			connect.sendMessage(message);
		}
	}

	/**
	 * Broadcast a message to all connected users
	 * @deprecated
	 * @param message
	 */
	public static void broadcast(NetworkMessage message) {
		for (Connection conn : connectedUser) {
			conn.sendMessage(message);
		}
	}
}
