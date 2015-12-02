package controllers;

import core.type.NetworkStatus;
import core.type.NetworkMessage;
import play.*;
import play.mvc.*;
import play.mvc.Http.*;
import static play.libs.F.Matcher.*;
import static play.mvc.Http.WebSocketEvent.*;

import core.*;
import play.libs.F;

public class Socket extends WebSocketController {

	public static void connect() {
		
		String username = session.get("user");
		username = "Developpeur"; // TODO developpement trick
		if (username == null) { 
			Authenticate.login();
		}
		
		Connection user = ConnectionManager.openConnection(username);
		// username = "Developpeur"; // TODO developpement trick
		Logger.info("New user : '" + username + "' opened a web socket");


		if (user == null) {
			//outbound.send("{\"header\":{\"dest\":\"dbg_cide\",\"src\":\"core.ConnectionManager\"},\"body\":{\"error\":{\"message\":\"User not found\"}}}");
			NetworkStatus err = new NetworkStatus(username, "core.ConnectionManager", NetworkStatus.Status.UserNotFound);
			outbound.send(err.getJSON());
			return;
		}
		NetworkStatus opn = new NetworkStatus(username, "core.ConnectionManager", NetworkStatus.Status.UserConnected);
		outbound.send(opn.getJSON());

		while (inbound.isOpen()) {
			F.Promise<F.Either<WebSocketEvent, NetworkMessage>> event;
			event = F.Promise.waitEither(inbound.nextEvent(), user.nextOutbountEvent());
			F.Either<WebSocketEvent, NetworkMessage> e = WebSocketController.await(event);

			Logger.debug("[" + username + "] Catch event: " + e.toString());
			if (e._1.isDefined()) {
				// Receive message
				WebSocketEvent wse = e._1.get();

				for (String quit : TextFrame.and(Equals("quit")).match(wse)) {
					Logger.debug("[" + username + "] WebSocket request disconnect");
					ConnectionManager.closeConnection (username);
					return;
					// TODO Repare - disconnect();
				}

				for (String msg : TextFrame.match(wse)) {
					Logger.info("[" + username + "] Message recive: " + msg);
					NetworkMessage nMsg = NetworkMessage.parseJSON(msg);
					if (nMsg != null) {
						nMsg.header.user = username;
						PluginManager.dispatchNetworkMessage(nMsg);
					}
				}

				for (WebSocketClose closed : SocketClosed.match(wse)) {
					Logger.debug("[" + username + "] WebSocket closed!");
				}
			} else if (e._2.isDefined()) {
				// Sending message
				Logger.debug("[" + username + "] Sending message");
				outbound.send(e._2.get().getJSON());
			}
		}
	}
}
