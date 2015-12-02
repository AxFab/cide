package core.net.type.messages;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.Project;
import models.User;

import com.google.gson.annotations.Expose;

public class NetworkFile implements Comparable {
	
	@Expose
	public String filepath;
	
	@Expose
	public List<String> content;
	
	@Expose
	public String extension;
	
	@Expose
	public boolean isDirectory;
	
	@Expose
	public List<String> usernames;
	
	@Expose
	public Map<User, Set<Integer>> locks;
	
	/**
	 * Builds a new NetworkFile, with the content specified
	 * @param filepath
	 * @param content
	 */
	public NetworkFile(String filepath, List<String> content) {
		this.filepath = filepath;
		this.content = new ArrayList<String> (content);
		this.usernames = new ArrayList<>();
		this.extension = this.getExtension(filepath);
		this.locks = new HashMap<>();
	}
	
	/**
	 * Builds a new NetworkFile
	 */
	public NetworkFile() {
		this.content = new ArrayList<>();
		this.usernames = new ArrayList<>();
		this.locks = new HashMap ();
	}
	
	
	/**
	 * Builds a new NetworkFile, stripping the original absolute path
	 * Used for the Filetree
	 * @param project
	 * @param filepath
	 */
	public NetworkFile(Project project, File file) {
		
		// TODO: 
		this.filepath = project.root.toURI().relativize(file.toURI()).toString();		
		this.extension = getExtension(file.getName());
		if(file.isDirectory()) {
			this.isDirectory = true;
			this.filepath = filepath.substring(0, filepath.length()-1);
		}
	}
	

	/**
	 * TODO: Deduce the FileExtension class instead of String
	 * Extract the extension from the file
	 * 
	 * @return
	 */
	private String getExtension(String filepath) {

		int dotPos = filepath.lastIndexOf(".");

		if (dotPos == -1) {
			return "";
		}

		return filepath.substring(dotPos + 1);
	}

	@Override
	public int compareTo(Object object) {
		
		if(object instanceof NetworkFile) {
			
			NetworkFile file = (NetworkFile) object;
			
			return this.filepath.compareTo(file.filepath);
		}

		return 0;
	}
}