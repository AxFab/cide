package core.module.type;

import java.io.File;
import java.io.IOException;
import java.util.List;

import models.Project;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import play.Logger;

import core.net.type.messages.NetworkFile;

public class SourceFile {
	
	public String filePath;
	private File file;
	
	private List<String> content;

	/**
	 * Create a sourceFile for a given file or directory
	 * 
	 * @param project
	 * @param filePath
	 * @return
	 * @throws IOException 
	 */
	public SourceFile(Project project, String filePath) throws IOException {
		constructor(project, filePath, false);
	}
	
	/**
	 * Create a sourceFile for a given file or directory
	 * 
	 * @param project
	 * @param filePath
	 * @param createFile Create the file if it doesn't exist already
	 * @return
	 * @throws IOException 
	 */
	public SourceFile (Project project, String filePath, boolean createFile) throws IOException {
		constructor(project, filePath, createFile);
	}
	
	private void constructor (Project project, String filePath, boolean createFile) throws IOException {
		this.filePath = filePath;
		
		this.file = new File(project.root.getAbsolutePath() + "/" + filePath);
		
		if (createFile) {
			FileUtils.touch(file);
		}
		
		if (!this.file.isDirectory()) {
			content = FileUtils.readLines(this.file, "UTF-8");
			Logger.debug("[File content] " + content);
		} else {
			throw new IOException("This file is a directory.");
		}
	}

	/**
	 * Show filepath when converted to string
	 */
	@Override
	public String toString() {

		return filePath;
	}
	
	public String getLine(int row) {
		if(row == content.size()) {
			content.add(row, "");
		}
		return content.get(row);
	}
	
	public void setLine(int row, String line) {
		if(row == content.size()) {
			content.add(row, "");
		}
		// TODO: Check bitch!
		line.replaceAll("\n", "");
		content.set(row, line);
	}
	
	/**
	 * Insert a new line after line row. 
	 * @param row
	 */
	public void insertLine(int row) {
		content.add(row, "");
	}
	
	/**
	 * Insert a new line after line row. 
	 * @param row
	 */
	public void insertLine(int row, String line) {
		content.add(row, line);
	}
	
	/**
	 * Removes a line from the SourceFile 
	 * @param row
	 */
	public void removeLine(int row) {
		if(row == content.size()) {
			return;
		}
		content.remove(row);
	}
	
	/**
	 * Update file on disk
	 * @throws IOException
	 */
	public void flush () throws IOException {
		FileUtils.writeLines(file, content);
	}
	
	/**
	 * Delete the source file
	 */
	public void delete () throws IOException {
		file.delete();
	}
	
	/**
	 * Get a NetworkFile containing the content of the SourceFile
	 * @throws IOException 
	 */
	public NetworkFile getNetworkFile () throws IOException {
		return new NetworkFile(filePath, content);
	}
}
