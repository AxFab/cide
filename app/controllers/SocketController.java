package controllers;

import static play.mvc.Http.WebSocketEvent.TextFrame;

import java.util.UUID;

import models.Project;
import models.User;

import play.Logger;
import play.libs.F;
import play.mvc.Before;
import play.mvc.Http.WebSocketEvent;
import play.mvc.WebSocketController;
import core.common.controller.ModuleManager;
import core.common.type.Event;
import core.net.controller.ConnectionManager;
import core.net.type.core.Connection;
import core.net.type.core.NetworkMessage;
import play.mvc.Http.WebSocketClose;

public class SocketController extends WebSocketController {

	/**
	 * Interceptor to properly unregister the connection in CIDE before the
	 * WebSocket is actually closed by Play!
	 */
	@Before(only = "disconnect")
	public static void disconnectWebsocket() {

		User user = User.find("username = ?", session.get("user")).first();
		Logger.debug("Un-registering the connection before closing the WebSocket.");
		ConnectionManager.closeConnection(user.username);
	}

	@Before
	public static void checkLogin() {

		if (session.get("user") == null) {
			UserController.login();
		}
	}

	/**
	 * Open websocket create by client
	 */
	public static void connect() {

		String username = session.get("user");

		User user = User.find("username = ?", username).first();
		if (user == null) {

			Logger.error("[SocketController] Unable to open socket: user '%s' not found",
					username);

			return;
		}
		
		// Disconnect the previous connections from this user
		ConnectionManager.closeConnection(user.username);

		UUID uuid = UUID.fromString(session.get("currentProject"));

		// Get the current Project
		Project project = Project.find("uuid = ?", uuid).first();
		if (project == null) {

			Logger.error("[SocketController] Unable to open socket: "
					+ "no project is associated with UUID '%s'", user.username);

			// Return error to client
			NetworkMessage err = new NetworkMessage(username, "Socket", "main",
					"ProjectNotFound", null);
			outbound.send(err.getJSON());
			
			return;
		}

		ModuleManager mm = ModuleManager.getInstance(project);

		// Register connection
		Connection connection = ConnectionManager.openConnection(user.username);
		Logger.info("O = '%s' + '%s'", user.username, project.name);

		//NetworkMessage message = new NetworkMessage(username, "net.main",
		//		SocketController.class.getName(), "SocketOpened", username);
		
		//ConnectionManager.sendMessage(message);
		
		while (inbound.isOpen()) {

			F.Promise<F.Either<WebSocketEvent, NetworkMessage>> event;
			event = F.Promise.waitEither(inbound.nextEvent(),
					connection.nextOutbountEvent());

			// Waits for an event from either the websocket or from Connection
			F.Either<WebSocketEvent, NetworkMessage> e = WebSocketController
					.await(event);

			if (e._1.isDefined()) { // Event from websocket

				// Receive message
				WebSocketEvent wse = e._1.get();
				
				// If we catch a close event, we unregister the WebSocket
				if(wse instanceof WebSocketClose) {
					
					Event closeEvent = new Event("userExit", user.username);
					mm.dispatchEvent(closeEvent);
					
					NetworkMessage closeMsg = new NetworkMessage(null, "*", SocketController.class.toString(), "userExit", user.username);
					ConnectionManager.broadcast(closeMsg);
					ConnectionManager.closeConnection(user.username);
				}

				// NetworkMessage received
				for (String msg : TextFrame.match(wse)) {

					Logger.info("R < '%s': " + msg, username);
					NetworkMessage nMsg = NetworkMessage.parseJSON(msg);
					if (nMsg != null) {
						
						nMsg.header.username = username;

						mm.dispatchNetworkMessage(nMsg);
					}
				}

			} else if (e._2.isDefined()) { // Event from Connection

				String data = e._2.get().getJSON();

				// Sending message
				Logger.debug("S > '%s': " + data, username);
				outbound.send(data);
			}
		}

		// Logger.error("WEBSOCKET HAS BEEN CLOSED1111!!!!! FFFFFFFUUUUUUUUUUUUUUUUUU!");
	}
}
