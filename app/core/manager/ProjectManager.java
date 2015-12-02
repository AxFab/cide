package core.manager;

import core.ConnectionManager;
import core.type.NetworkMessage;
import core.interfaces.IPlugin;
import core.type.NetworkBody;
import core.type.NetworkHeader;
import core.type.NetworkStatus;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;

import data.Event;
import data.Project;
import play.Logger;

public class ProjectManager implements IPlugin {

	List<Project> loadedProjects;

	public ProjectManager() {
	}

	@Override
	public void start() {
		loadedProjects = new LinkedList<Project>();
		Project proj = new Project("hello", "Cide.Language.CMakefile", null);
		loadedProjects.add(proj);
		// Load project
	}

	public String id() {
		return "Cide.ProjectManager";
	}

	/*
	 * List of catched event : 
	 *  - getFileTree
	 *  - openProject
	 */
	@Override
	public void onNetworkMessage(NetworkMessage msg) {
		Logger.debug("Project Manager received a event");

		if (msg.body.message.equals("openProject")) {
			if (msg.body.object instanceof String) {
				openProject(msg.header.user, msg.header.source, (String) msg.body.object);
			} else {
				NetworkStatus.sendStatus(msg.header.user, id(), NetworkStatus.Status.ErronedNetworkMessage);
				Logger.error("Project manager received erroned message: " + msg.getJSON());
			}
		} else if (msg.body.message.equals("getFileTree")) {
		} else if (msg.body.message.equals("Project C")) {
			ConnectionManager.sendMessage(msg);
		}
	}

	@Override
	public void onEvent(Event evt, Object data) {
		throw new NotImplementedException();
	}

	void openProject(String user, String destination, String project) {
		for (Project p : loadedProjects) {
			if (p.name.equals(project)) {
				NetworkHeader head = new NetworkHeader(user, destination, id());
				NetworkBody body = new NetworkBody("OpenedProject", p.getObj());
				ConnectionManager.sendMessage(new NetworkMessage(head, body));
				return;
			}
		}
		NetworkStatus.sendStatus(user, id(), NetworkStatus.Status.ErronedNetworkMessage);
	}
}
