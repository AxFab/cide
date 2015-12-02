package core.module.type;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import play.Logger;

import name.fraser.neil.plaintext.diff_match_patch;
import name.fraser.neil.plaintext.diff_match_patch.Patch;
import name.fraser.neil.plaintext.diff_match_patch.Operation;

import models.Project;
import models.User;

import core.module.type.LockingException.LockingExceptionType;
import core.net.type.messages.NetworkFile;
import core.net.type.messages.NetworkPatch;

public class CollabFile {
	private Map<User, Position> positions;

	/** Users can each lock a line */
	private Map<User, Set<Integer>> locks;
	/** Changes to sent to users */
	private Map<User, Map<Integer, LinkedList<Patch>>> patches;
	/** A sequential list a changes to lines */
	private Map<User, List<LineDiff>> lineChanges;

	private Project project;
	private SourceFile file;
	
	private int updateCounter = 0;
	private static final int COUNTER_MAX = 5;
	
	private static diff_match_patch dmp;
	
	public CollabFile (Project project, String filePath) throws IOException {
		this.project = project;
		this.file = new SourceFile(project, filePath);
		
		this.positions = new HashMap<> ();
		this.locks = new HashMap<> ();
		this.patches = new HashMap<User, Map<Integer, LinkedList<Patch>>>();
		this.lineChanges = new HashMap <User, List<LineDiff>> ();
		
		this.dmp = new diff_match_patch ();
	}
	
	/**
	 * Check if a project and filepath define the current CollabFile
	 * @param project
	 * @param filePath
	 * @return
	 */
	public boolean isSameAs (Project project, String filePath) {
		return this.file.filePath.equals(filePath) && this.project.equals(project); 
	}
	
	/**
	 * Add a user as working on the file
	 * @param user
	 */
	public boolean addUser(User user) {
		if (!positions.containsKey(user)) {
			positions.put(user, new Position(0,0));
			locks.put(user, new HashSet());
			patches.put(user, new HashMap<Integer, LinkedList<Patch>>());
			lineChanges.put(user, new LinkedList<LineDiff> ());
			return true;
		}
		return false;
	}
	
	/**
	 * Remove a user as working on the file
	 * @param user
	 */
	public void remUser(User user) {
		
		positions.remove(user);
		locks.remove(user);
		patches.remove(user);
		lineChanges.remove(user);
		
		//TODO remove file from memory when all users close
	}
	
	/**
	 * Return a set of the users editing the file
	 * @return
	 */
	public Set<User> getUsers() {
		return positions.keySet();
	}
	
	/**
	 * Return the number of users editing the file
	 * @return
	 */
	private int numUsers () {
		return positions.keySet().size();
	}
	
	/**
	 * Update a user's position
	 * @param user
	 * @param pos
	 */
	public void setPosition (User user, Position pos) {
		positions.put(user, pos);
	}
	/**
	 * Return every user's position
	 * @return
	 */
	public Map<User, Position> getPositions () {
		return positions;
	}
	
	/**
	 * Set a lock on a line for a user
	 * TODO: Remove explicitly setting locks
	 * @throws LockingException 
	 */
	public void setLock(User user, int row) throws LockingException {
		
		// Check that no other user is already locking the file
		for (User u : locks.keySet()) {
			if (!u.equals(user) && locks.get(u).contains(row)) {
				throw new LockingException (LockingExceptionType.ALREADY_LOCKED, row, u.username);
			}
		}
		locks.get(user).add(row);
	}
	
	/**
	 * Check if the user can edit the given line and throw an exception otherwise
	 * @param user
	 * @param row
	 * @throws LockingException 
	 */
	private void checkLock (User user, int row) throws LockingException {
		// Check the lock
		if (!locks.get(user).contains(row)) {
			for (User u : locks.keySet()) {
				if (!u.equals(user) && locks.get(u).contains(row)) {
					throw new LockingException (LockingExceptionType.OBTAIN_LOCK, row, u.username);
				}
			}
			// Nobody else is holding the lock
			throw new LockingException (LockingExceptionType.OBTAIN_LOCK, row, null);
		}
	}
	
	/**
	 * Release a lock for a user
	 */
	public boolean releaseLock(User user, int row) {
		return locks.get(user).remove(row);
	}
	
	/**
	 * Apply a diff to a line
	 * @param user
	 * @param row
	 * @param linePatches
	 */
	public void applyLinePatch (User user, int row, LinkedList<Patch> linePatches) throws IOException, LockingException {
		checkLock(user, row);
		
		String oldLine = file.getLine(row);
		
		Object[] appliedPatches = dmp.patch_apply(linePatches, oldLine);
		
		String newLine = (String)(appliedPatches[0]);
		boolean[] results = (boolean[]) appliedPatches[1];

		for(boolean b: results) {
			if (!b) {
				Logger.error ("[CollabFile] Applying patch from %s on %s at line %d failed", 
						user.username, file.filePath, row);
			}
		}
		
		// Update main file
		file.setLine(row, newLine);
		
		// Write to disk if needed
		flush();
		
		if (this.numUsers() == 1) {
			return;
		}
		
		// Update other users' patches
		for (User u : patches.keySet()) {
			Map<Integer, LinkedList<Patch>> user_patches = patches.get(u);
			
			// The user who applied the patch has the latest version
			if (u.equals(user)) {
				user_patches.remove(row);
				continue;
			}
			
			if (user_patches.containsKey(row)) {
				user_patches.get(row).addAll(linePatches);
			} else {
				user_patches.put(row, new LinkedList<Patch>(linePatches));
			}
		}
	}
	
	private void flush() throws IOException {
		
		file.flush();
		
		if(++updateCounter % COUNTER_MAX == 0) {

			updateCounter = 0;
			
			file.flush();
		}
		
		Logger.debug("[CollabFile] UpdateCounter: "+updateCounter);
	}

	/**
	 * Set a line's content, applyLinePatch is preferred
	 * @throws LockingException 
	 */
	public void setLine (User user, int row, String newLine) throws IOException, LockingException {
		checkLock(user, row);
		
		String oldLine = file.getLine(row);
		LinkedList<Patch> linePatches = dmp.patch_make(oldLine, newLine);
		
		applyLinePatch (user, row, linePatches);
	}
	
	/**
	 * Insert a new line after the given line
	 */
	public void insertLine (User user, int row) {
		insertLine(user, row, "");
	}
	
	/**
	 * Insert a new line with text after the given line
	 */
	public void insertLine (User user, int row, String newLine) {
		file.insertLine(row, newLine);
		
		// Add new line to other users' patches
		for (User u : lineChanges.keySet()) {
			if (!u.equals(user)) {
				lineChanges.get(u).add (new LineDiff(Operation.INSERT, row));
			}
		}
		
		//Update locks
		for (User u: locks.keySet()) {
			for (Integer lockRow: locks.get(u)) {
				if (lockRow > row) {
					Logger.debug("<><> Moving lock on line %d to line %d", lockRow, lockRow+1);
					lockRow += 1;
				}
			}
		}
	}
	
	/**
	 * Remove a line
	 * @throws LockingException 
	 */
	public void removeLine (User user, int row) throws LockingException {
		checkLock(user, row);
		
		file.removeLine(row);
		
		// Remove line for other users
		for (User u : lineChanges.keySet()) {
			if (!u.equals(user)) {
				lineChanges.get(u).add (new LineDiff(Operation.DELETE, row));
			}
		}
		
		//Update locks
		for (User u: locks.keySet()) {
			for (Integer lockRow: locks.get(u)) {
				if (lockRow > row) {
					Logger.debug("<><> Moving lock on line %d to line %d", lockRow, lockRow-1);
					lockRow -= 1;
				}
			}
		}
	}
	
	/**
	 * Return a NetworkPatch to bring the user up to date with the server's 
	 * version of the file and consider that the user applied the patches
	 */
	public NetworkPatch getNetworkPatch (User user) {
		List<LineDiff> user_lineChanges = lineChanges.get(user);
		Map<Integer, LinkedList<Patch>> user_patches = patches.get(user);
		Map<Integer, String> user_string_patches = new HashMap<>();
		
		// Convert patches to string format
		for (Integer i: user_patches.keySet()) {
			user_string_patches.put(i, dmp.patch_toText(user_patches.get(i)));
		}
		
		NetworkPatch np = new NetworkPatch(user_string_patches, user_lineChanges);
		
		user_patches.clear();
		user_lineChanges.clear();
		
		return np;
	}
	
	/**
	 * Return a NetworkFile containing the entire file and save the user as 
	 * being up to date
	 * @throws IOException 
	 */
	public NetworkFile getNetworkFile (User user) throws IOException {
		patches.get(user).clear();
		lineChanges.get(user).clear();
		
		NetworkFile networkFile = file.getNetworkFile();
		
		// Add current user positions
		for(User currentUser: positions.keySet()) {
			networkFile.usernames.add(currentUser.username);
		}
		
		// Add current locks
		networkFile.locks.putAll(this.locks);
		
		return networkFile;
	}
	
	/**
	 * Get the relative SourceFile path
	 * @return
	 */
	public String getFilePath() {
		
		return this.file.filePath;
	}
	
	public boolean deleteFile() throws IOException {
		
		if(this.getUsers().size() == 1) {
			this.file.delete();
			return true;
		}
		
		return false;
	}
}
