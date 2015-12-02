package core.module.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import models.Project;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.NotImplementedException;

import play.Logger;
import play.mvc.Scope.Session;

import com.google.gson.JsonSyntaxException;

import core.common.controller.ModuleManager;
import core.common.controller.SettingsManager;
import core.common.type.CideException;
import core.common.type.Event;
import core.common.type.Module;
import core.module.type.CollabFile;
import core.module.type.SourceFile;
import core.net.controller.ConnectionManager;
import core.net.type.core.NetworkMessage;
import core.net.type.messages.NetworkChatMessage;
import core.net.type.messages.NetworkFile;

public class ProjectModule extends Module {

	@Override
	public void onRegister(ModuleManager mm) throws CideException {

		this.mm = mm;

		mm.project.root = new File(SettingsManager.getValue("rootPath") + "projects/" + mm.project.uuid);

		Logger.debug("[ProjectModule] Getting filesystem tree from: '%s'", mm.project.root);

		if (!mm.project.root.exists()) {

			// Create the directory and copy everything in place
			try {
				mm.project.type.initializeProject(mm.project);
			} catch (IOException e) {
				throw new CideException("Couldn't open the folder of the project '" + mm.project.name + "'", e);
			}
		}

		// Register this module to events
		mm.subscribeEvent("refreshFileTree", this);
	}

	@Override
	public void onUnregister() {

		throw new NotImplementedException();
	}

	/**
	 * List of caught NetworkMessage: TODO
	 **/
	@Override
	public void onNetworkMessage(NetworkMessage msg) throws CideException {

		// Invert source and destination
		NetworkMessage answer = msg.createAnswer();

		switch (msg.body.message) {

		case "getFileTree":
			try {
				answer.body.message = "projectFileTree";
				answer.body.object = getFileTree(msg.body.getObject(String.class));
			} catch (IOException e) {
				Logger.error("Unable to list project files");
				e.printStackTrace();

				answer.body.message = "fileError";
				answer.body.object = String.format("Error listing files for project \"%s\"", mm.project.name);
			}
			break;

		case "newFolder":
			try {
				newFolder(msg.body.getObject(String.class));
				answer.body.message = "refreshFileTree";
				answer.body.object = getFileTree("/");
			} catch (IOException e) {
				throw new CideException("Couldn't add the folder in the FS", e);
			}
			break;

		case "newFile":
			try {
				newFile(msg.body.getObject(String.class));
				answer.body.message = "refreshFileTree";
				answer.body.object = getFileTree("/");
			} catch (IOException e) {
				throw new CideException("Couldn't add the file in the FS", e);
			}

			break;

		default:
			Logger.error("[ProjectModule] Unsupported NetworkMessage received '%s'", msg.body.message);
		}

		// If the message is set, we can send the NetworkMessage
		if (!answer.body.message.isEmpty()) {
			ConnectionManager.sendMessage(answer);
		}
	}

	@Override
	public void onEvent(Event evt) throws CideException {

		switch (evt.message) {

		case "refreshFileTree":

			NetworkMessage answer = new NetworkMessage(evt.object.toString(), "browser",
					ProjectModule.class.toString(), "refreshFileTree", "");
			try {
				answer.body.object = getFileTree("/");

				ConnectionManager.sendMessage(answer);
				
			} catch (IOException e) {
				
				Logger.error(e, "Unable to list project files");
				throw new CideException("I'm unable to get the list of files in the project root.");
			}
			break;
		}
	}

	/**
	 * Creates a folder in the project
	 * 
	 * @param filepath
	 * @throws IOException
	 */
	private void newFolder(String filepath) throws IOException {

		File newFolder = new File(mm.project.root + File.separator + filepath);
		// XXX: Could use .. to go back in the folders, use relativize
		Logger.debug("Trying to create folder named '%s'", filepath);

		if (!newFolder.exists()) {
			newFolder.mkdirs();
		} else {
			throw new IOException("The folder you specified already exists!");
		}
	}

	/**
	 * Add a file in the project
	 * 
	 * @param filepath
	 * @throws IOException
	 */
	private void newFile(String filepath) throws IOException {

		File newFile = new File(mm.project.root + File.separator + filepath);
		// XXX: Could use .. to go back in the folders, use relativize

		if (!newFile.exists()) {
			FileUtils.touch(newFile);
		} else {
			throw new IOException("The folder you specified already exists!");
		}
	}

	/**
	 * Return the project file-tree
	 * 
	 * @return
	 * @throws IOException
	 */
	private Collection<NetworkFile> getFileTree(String path) throws IOException {

		// Get the list of files in the project folder
		List<NetworkFile> networkFiles = new ArrayList<>();
		List<NetworkFile> networkFolders = new ArrayList<>();
		Collection<File> files = new LinkedList<>();

		if (path.equals("/")) {
			path = "";
		}
		File folder = new File(mm.project.root, path);

		if (folder.isDirectory())
			files.addAll(Arrays.asList(folder.listFiles()));

		for (File file : files) {

			if (file.isHidden()) {
				continue;
			}

			if (file.getName().contains("~")) {
				continue;
			}

			if (file.isDirectory()) {

				NetworkFile networkFile = new NetworkFile(mm.project, file);
				networkFile.isDirectory = true;

				networkFolders.add(networkFile);

			} else {
				NetworkFile networkFile = new NetworkFile(mm.project, file);
				networkFile.isDirectory = false;

				networkFiles.add(networkFile);
			}
		}

		// Sort
		Collections.sort(networkFiles);
		Collections.sort(networkFolders);

		networkFolders.addAll(networkFiles);

		return networkFolders;
	}
}
