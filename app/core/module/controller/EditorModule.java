package core.module.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import models.User;
import name.fraser.neil.plaintext.diff_match_patch;
import name.fraser.neil.plaintext.diff_match_patch.Patch;
import play.Logger;
import play.libs.Files;
import core.common.controller.ModuleManager;
import core.common.type.CideException;
import core.common.type.Event;
import core.common.type.Module;
import core.module.type.CollabFile;
import core.module.type.LockingException;
import core.module.type.Position;
import core.net.controller.ConnectionManager;
import core.net.type.core.NetworkMessage;
import core.net.type.messages.NetworkChatMessage;
import core.net.type.messages.NetworkCursorPosition;
import core.net.type.messages.NetworkFile;
import core.net.type.messages.NetworkLine;
import core.net.type.messages.NetworkLineLock;
import core.net.type.messages.NetworkLinePatch;

public class EditorModule extends Module {

	public static Set<CollabFile> files = new HashSet<CollabFile>();
	private static diff_match_patch dmp = new diff_match_patch();
	public ModuleManager mm;

	public void onRegister(ModuleManager mm) {
		super.mm = mm;

		mm.subscribeEvent("userExit", this);
	}

	public void onUnregister() {

	}

	public void onNetworkMessage(NetworkMessage msg) throws CideException {

		NetworkMessage answer = msg.createAnswer();
		User user = User.getUser(msg.header.username);

		switch (msg.body.message) {

		case "getSourceFile": {

			// XXX: This sucks balls
			String filepath = msg.body.getObject(String.class);
			try {
				answer.body.message = "sourceFile";
				NetworkFile networkFile = openFile(user, filepath);

				// Once the file is opened, tell the users
				CollabFile currentFile = findFile(filepath);

				List<String> usernames = new ArrayList<>(networkFile.usernames);

				for (ListIterator<String> it = networkFile.usernames
						.listIterator(); it.hasNext();) {
					if (it.next().equals(msg.header.username)) {
						it.remove();
						break;
					}
				}

				answer.body.object = networkFile;

				// NetworkFile to send to everyone containing only the usernames
				for (User currentUser : currentFile.getUsers()) {

					NetworkFile lightNetworkFile = new NetworkFile();

					lightNetworkFile.usernames.addAll(usernames);

					NetworkMessage fileMsg = new NetworkMessage("",
							answer.header.destination, answer.header.source,
							"userOpenedFile", lightNetworkFile);

					fileMsg.header.username = currentUser.username;

					for (ListIterator<String> it = lightNetworkFile.usernames
							.listIterator(); it.hasNext();) {
						if (it.next().equals(currentUser.username)) {
							it.remove();
							break;
						}
					}

					ConnectionManager.sendMessage(fileMsg);
				}

			} catch (IOException e) {

				throw new CideException("Error opening file " + filepath, e);
			}
		}
			break;

		case "lockLine": {

			NetworkLineLock lineLock = msg.body
					.getObject(NetworkLineLock.class);

			CollabFile currentFile = findFile(lineLock.filepath);
			if (currentFile == null) {
				throw new CideException("The file '" + lineLock.filepath
						+ "' is not opened for collaboration!");
			}

			try {

				Logger.debug("'%s' is trying to lock line %s",
						msg.header.username, lineLock.row);

				currentFile.setLock(User.getUser(msg.header.username),
						lineLock.row);
				answer.body.message = "lineLocked";
				answer.body.object = lineLock;

				// Tell all the users we locked the line
				NetworkMessage lockMsg = new NetworkMessage("",
						answer.header.destination, answer.header.source,
						"userLockedLine", lineLock);
				for (User currentUser : currentFile.getUsers()) {

					if (!currentUser.username.equals(msg.header.username)) {

						lockMsg.header.username = currentUser.username;
						ConnectionManager.sendMessage(lockMsg);
					}
				}

			} catch (LockingException ex) {

				answer.body.message = "lineAlreadyLockedError";
				answer.body.object = ex;
			}
		}

			break;

		case "applyLinePatch": {

			NetworkLinePatch linePatch = msg.body
					.getObject(NetworkLinePatch.class);

			CollabFile currentFile = findFile(linePatch.filepath);
			if (currentFile == null) {
				throw new CideException("The file '" + linePatch.filepath
						+ "' is not opened for collaboration!");
			}

			LinkedList<Patch> patches = new LinkedList(
					dmp.patch_fromText(linePatch.patches));
			try {
				currentFile.applyLinePatch(User.getUser(msg.header.username),
						linePatch.row, patches);

				// Send patches to every user on the file

				for (User currentUser : currentFile.getUsers()) {
					NetworkMessage patchMsg = new NetworkMessage("",
							answer.header.destination, answer.header.source,
							"userChangedFile", null);

					patchMsg.header.username = currentUser.username;
					patchMsg.body.object = currentFile
							.getNetworkPatch(currentUser);

					ConnectionManager.sendMessage(patchMsg);
				}
			} catch (IOException e) {
				throw new CideException("Couldn't save the changes to file "
						+ linePatch.filepath, e);
			} catch (LockingException ex) {

				answer.body.message = "lineAlreadyLockedError";
				answer.body.object = ex;
			}
		}
			break;

		case "unlockLine": {

			NetworkLine line = msg.body.getObject(NetworkLine.class);

			CollabFile currentFile = findFile(line.filepath);
			if (currentFile == null) {
				throw new CideException("The file '" + line.filepath
						+ "' is not opened for collaboration!");
			}

			currentFile
					.releaseLock(User.getUser(msg.header.username), line.row);

			// Tell all the users we unlocked the line
			NetworkLineLock lineLock = new NetworkLineLock(line.filepath,
					line.row, null);
			NetworkMessage unlockMsg = new NetworkMessage("",
					answer.header.destination, answer.header.source,
					"userUnlockedLine", lineLock);
			for (User currentUser : currentFile.getUsers()) {

				if (!currentUser.username.equals(msg.header.username)) {

					unlockMsg.header.username = currentUser.username;
					ConnectionManager.sendMessage(unlockMsg);
				}
			}
		}
			break;

		case "setCursor": {

			NetworkCursorPosition cursorPosition = msg.body
					.getObject(NetworkCursorPosition.class);
			CollabFile currentFile = findFile(cursorPosition.filepath);
			if (currentFile == null) {
				throw new CideException("The file '" + cursorPosition.filepath
						+ "' is not opened for collaboration!");
			}

			currentFile.setPosition(user, new Position(cursorPosition));

			// Broadcast new position to all users
			for (User currentUser : currentFile.getUsers()) {

				// Logger.debug("[EditorModule] User '%s'", currentUser);

				if (!currentUser.username.equals(msg.header.username)) {

					Logger.debug(
							"[EditorModule] Sending cursor position of %s to %s",
							msg.header.username, currentUser);

					// Internal NetworkMessage because it's asynchronous
					NetworkMessage posMsg = new NetworkMessage(
							answer.header.username, answer.header.destination,
							answer.header.source, "setForeignCursor", null);

					Position userPosition = currentFile.getPositions().get(
							currentUser);
					posMsg.header.username = currentUser.username;
					posMsg.body.message = "setForeignCursor";

					cursorPosition.username = msg.header.username;
					cursorPosition.filepath = cursorPosition.filepath;

					posMsg.body.object = cursorPosition;
					ConnectionManager.sendMessage(posMsg);
				}
			}

			answer.body.message = "";

			break;
		}

		case "insertLine": {

			NetworkLine line = msg.body.getObject(NetworkLine.class);
			CollabFile currentFile = findFile(line.filepath);
			if (currentFile == null) {
				throw new CideException("The file '" + line.filepath
						+ "' is not opened for collaboration!");
			}
			currentFile.insertLine(User.getUser(msg.header.username), line.row);

			answer.body.message = "";

			break;
		}

		case "removeLine": {

			NetworkLine line = msg.body.getObject(NetworkLine.class);
			CollabFile currentFile = findFile(line.filepath);
			if (currentFile == null) {
				throw new CideException("The file '" + line.filepath
						+ "' is not opened for collaboration!");
			}
			currentFile.removeLine(User.getUser(msg.header.username), line.row);
			Logger.debug("Removing line " + line.row);

			answer.body.message = "";

			break;
		}

		case "closeFile": {

			String filepath = msg.body.getObject(String.class);

			// Once the file is opened, tell the users
			CollabFile currentFile = findFile(filepath);

			NetworkFile networkFile = new NetworkFile();
			networkFile.usernames.add(msg.header.username);
			networkFile.filepath = filepath;

			NetworkMessage fileMsg = new NetworkMessage("",
					answer.header.destination, answer.header.source,
					"userClosedFile", networkFile);

			for (User currentUser : currentFile.getUsers()) {
				fileMsg.header.username = currentUser.username;
				ConnectionManager.sendMessage(fileMsg);
			}

			// Remove the user
			currentFile.remUser(User.getUser(msg.header.username));

			// If we were the last one
			if (currentFile.getUsers().size() == 0) {
				files.remove(currentFile);
			}

			break;
		}

		case "deleteFile":

			try {

				String filepath = msg.body.getObject(String.class);
				// Check if we can delete the file
				CollabFile currentFile = findFile(filepath);

				if (currentFile == null) {
					File fileToDelete = new File(mm.project.root
							+ File.separator + filepath);
					Logger.debug("Deleting "+fileToDelete);
					if (fileToDelete.isDirectory()) {
						Files.deleteDirectory(fileToDelete);
					} else {
						Files.delete(fileToDelete);
					}
					Logger.debug("Removing file " + fileToDelete);
				} else {
					if (currentFile.deleteFile()) {
						files.remove(currentFile);
					} else {
						throw new IOException("Couldn't delete the file "
								+ filepath);
					}
				}
				Event event = new Event("refreshFileTree", user.username);
				mm.dispatchEvent(event);

			} catch (IOException ex) {
				throw new CideException(
						"There was an error while trying to delete the file!",
						ex);
			}

			break;

		case "chatMessage":

			NetworkChatMessage chatMessage = msg.body
					.getObject(NetworkChatMessage.class);
			CollabFile currentFile = findFile(chatMessage.filepath);
			if (currentFile == null) {
				throw new CideException("The file '" + chatMessage.filepath
						+ "' is not opened for collaboration!");
			}
			chatMessage.username = User.getUser(msg.header.username).firstname;

			answer.body.message = "chatMessage";
			answer.body.object = chatMessage;
			for (User curUser : currentFile.getUsers()) {
				answer.header.username = curUser.username;
				ConnectionManager.sendMessage(answer);
			}
			return;
		}

		if (!answer.body.message.isEmpty()) {
			ConnectionManager.sendMessage(answer);
		}
	}

	public void onEvent(Event evt) {
		Logger.error("Unsupported event received %s", evt.message);
	}

	/**
	 * Open a file if it not already open and switch to it in the editor
	 * 
	 * @param filePath
	 * @throws IOException
	 */
	private NetworkFile openFile(User user, String filePath)
			throws IOException, CideException {

		CollabFile fileToOpen = findFile(filePath);

		if (fileToOpen == null) {
			fileToOpen = new CollabFile(mm.project, filePath);
			files.add(fileToOpen);
		}

		fileToOpen.addUser(user);

		return fileToOpen.getNetworkFile(user);
	}

	/**
	 * Find an already opened file
	 * 
	 * @param filePath
	 * @return the file or null
	 */
	private CollabFile findFile(String filePath) {
		for (CollabFile f : files) {
			if (f.isSameAs(mm.project, filePath)) {
				return f;
			}
		}
		return null;
	}
}
