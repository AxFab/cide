package controllers;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import models.Project;

import org.apache.commons.io.FileUtils;

import core.common.controller.ModuleManager;
import core.model.FileExtension;
import play.Logger;
import play.mvc.Controller;

public class DialogController extends Controller {

	public static void get(String dialogId) {

		renderTemplate("@dialogs." + dialogId);
	}

	public static void projectProperties() {

		Map<String, String> props = new HashMap<>();

		Project currentProject = Project.find("uuid = ?",
				UUID.fromString(session.get("currentProject"))).first();

		renderArgs.put("project", currentProject);
		renderTemplate("@dialogs.project-properties");
	}

	/*
	 * public static void newFile() {
	 * 
	 * // Fetch the available project extensions Project currentProject =
	 * Project.find("uuid = ?",
	 * UUID.fromString(session.get("currentProject"))).first();
	 * 
	 * // Get the module manager ModuleManager mm =
	 * ModuleManager.getInstance(currentProject);
	 * 
	 * List<FileExtension> extensions = new ArrayList<>(); List<Language>
	 * languages = currentProject.type.langs; for (Language language :
	 * languages) {
	 * 
	 * extensions.addAll(language.extensions); }
	 * 
	 * renderArgs.put("extensions", extensions);
	 * 
	 * List<String> paths = new ArrayList<String>(); try { for (File file :
	 * getFileListing(mm.project.root)) {
	 * 
	 * Logger.debug(file.getName()); } } catch (FileNotFoundException e) { //
	 * TODO Auto-generated catch block e.printStackTrace(); }
	 * 
	 * renderTemplate("@dialogs.new-file"); }
	 * 
	 * public static void newFolder() {
	 * 
	 * // Fetch the available project extensions Project currentProject =
	 * Project.find("uuid = ?",
	 * UUID.fromString(session.get("currentProject"))).first();
	 * 
	 * // Get the module manager ModuleManager mm =
	 * ModuleManager.getInstance(currentProject);
	 * 
	 * List<String> paths = new ArrayList<String>(); try { for (File file :
	 * getFileListing(mm.project.root)) {
	 * 
	 * Logger.debug(file.getName()); } } catch (FileNotFoundException e) { //
	 * TODO Auto-generated catch block e.printStackTrace(); }
	 * 
	 * renderTemplate("@dialogs.new-folder"); }
	 * 
	 * public static void editorStyle() {
	 * 
	 * renderTemplate("@dialogs.editor-style"); }
	 * 
	 * static public List<File> getFileListing(File aStartingDir) throws
	 * FileNotFoundException { validateDirectory(aStartingDir); List<File>
	 * result = getFileListingNoSort(aStartingDir); Collections.sort(result);
	 * return result; }
	 * 
	 * // PRIVATE // static private List<File> getFileListingNoSort(File
	 * aStartingDir) throws FileNotFoundException { List<File> result = new
	 * ArrayList<File>(); File[] filesAndDirs = aStartingDir.listFiles();
	 * List<File> filesDirs = Arrays.asList(filesAndDirs); for (File file :
	 * filesDirs) { if (!file.isFile() && !file.isHidden()) { result.add(file);
	 * // must be a directory // recursive call! List<File> deeperList =
	 * getFileListingNoSort(file); result.addAll(deeperList); } } return result;
	 * }
	 * 
	 * static private void validateDirectory(File aDirectory) throws
	 * FileNotFoundException { if (aDirectory == null) { throw new
	 * IllegalArgumentException("Directory should not be null."); } if
	 * (!aDirectory.exists()) { throw new
	 * FileNotFoundException("Directory does not exist: " + aDirectory); } if
	 * (!aDirectory.isDirectory()) { throw new
	 * IllegalArgumentException("Is not a directory: " + aDirectory); } if
	 * (!aDirectory.canRead()) { throw new
	 * IllegalArgumentException("Directory cannot be read: " + aDirectory); } }
	 */
}
