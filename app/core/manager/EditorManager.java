package core.manager;

import core.ConnectionManager;
import core.interfaces.IPlugin;
import core.type.NetworkBody;
import core.type.NetworkHeader;
import core.type.NetworkMessage;
import data.EditorFile;
import data.Event;
import data.LineEditData;
import java.util.LinkedList;
import java.util.List;
import play.Logger;

public class EditorManager implements IPlugin {

	static List<EditorFile> openDocs = new LinkedList<EditorFile> ();
	
	public String id() {
		return "Cide.EditorManager";
	}

	public void start() {
	}

	public void onNetworkMessage(NetworkMessage msg) {
		
		if (msg.body.message.equals("openFile")) {
			EditorFile ef = new EditorFile ("main.c");
			openDocs.add (ef);
			
			ConnectionManager.sendMessage(new NetworkMessage(
				new NetworkHeader(msg.header.user, "aceEditor", id()), 
				new NetworkBody("file", ef)));
		}
		else {
			LineEditData edition = msg.body.getObject (LineEditData.class);
			Logger.debug ("Editor receive: l." + edition.line + " -> " + edition.text);
		}
	}

	public void onEvent(Event evt, Object data) {
	}
	
}
